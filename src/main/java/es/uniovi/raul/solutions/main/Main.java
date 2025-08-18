package es.uniovi.raul.solutions.main;

import static java.time.LocalDate.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import es.uniovi.raul.solutions.cli.*;
import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.github.GithubConnection.*;
import es.uniovi.raul.solutions.github.GithubConnectionImpl;
import es.uniovi.raul.solutions.schedule.ScheduleLoader;
import es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args) {

        Optional<Arguments> argumentsOpt = ArgumentsParser.parse(args);
        if (argumentsOpt.isEmpty())
            System.exit(1);

        try {

            run(argumentsOpt.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Operation was interrupted.");
            System.exit(1);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);

    }

    private static void run(Arguments arguments) throws UnexpectedFormatException,
            RejectedOperationException, IOException, InterruptedException, InvalidScheduleFormat {

        var schedule = loadSchedule(arguments.scheduleFile);
        var connection = new GithubConnectionImpl(arguments.token);
        var course = new Course(arguments.organization, connection, schedule);

        course.getAllSolutions().forEach(System.out::println);
        course.getGroups().stream().map(Group::name).forEach(System.out::println);

        System.out.println(guessAction(course));
    }

    private static boolean guessAction(Course course)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var guessedGroupOpt = guessGroup(course.getGroups());
        if (guessedGroupOpt.isEmpty())
            return false;

        var guessedSolutionOpt = guessSolution(guessedGroupOpt.get(), course.getAllSolutions());
        if (guessedSolutionOpt.isEmpty())
            return false;

        var guessedGroup = guessedGroupOpt.get();
        var guessedSolution = guessedSolutionOpt.get();

        if (!Console.askConfirmation(
                String.format("(now is %s %s). Proceed to show '%s' to group '%s'?",
                        today(),
                        format(currentTime()),
                        guessedSolution,
                        guessedGroup.name())))
            return false;

        guessedGroup.grantAccess(guessedSolution);

        return true;
    }

    private static Optional<Group> guessGroup(List<Group> groups) {

        var matchingGroups = groups.stream()
                .filter(group -> group.isScheduledFor(today(), currentTime()))
                .toList();

        return (matchingGroups.size() == 1)
                ? Optional.of(matchingGroups.get(0))
                : Optional.empty();
    }

    private static Optional<String> guessSolution(Group group, List<String> allSolutions) {

        return allSolutions.stream()
                .sorted()
                .filter(solution -> !group.isAccesible(solution))
                .findFirst();
    }

    private static Map<String, Schedule> loadSchedule(String scheduleFile)
            throws IOException, InvalidScheduleFormat {

        if (scheduleFile == null) {
            Console.printWarning("No schedule file specified, so automatic group detection will be disabled.");
            return Collections.emptyMap();
        }

        var schedules = ScheduleLoader.load(scheduleFile);
        if (schedules.isEmpty())
            throw new InvalidScheduleFormat(scheduleFile + " is empty.");

        return schedules;
    }

    private static String today() {
        return now().getDayOfWeek().toString().toLowerCase();
    }

    private static LocalTime currentTime() {
        return LocalTime.now();
    }

    private static String format(LocalTime time) {
        return time.toString().substring(0, 5); // HH:mm format
    }
}
