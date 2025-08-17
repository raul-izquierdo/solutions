package es.uniovi.raul.solutions.main;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.solutions.cli.*;
import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.github.GithubConnection.*;
import es.uniovi.raul.solutions.github.GithubConnectionImpl;
import es.uniovi.raul.solutions.schedule.ScheduleLoader;
import es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormatException;

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
            RejectedOperationException, IOException, InterruptedException, InvalidScheduleFormatException {

        var schedule = loadSchedule(arguments.scheduleFile);
        var connection = new GithubConnectionImpl(arguments.token);
        var organization = new Course(arguments.organization, connection, schedule);

        organization.getSolutions().forEach(System.out::println);
        organization.getGroups().stream().map(Group::name).forEach(System.out::println);

    }

    private static Map<String, Schedule> loadSchedule(String scheduleFile)
            throws IOException, InvalidScheduleFormatException {

        if (scheduleFile == null) {
            Console.printWarning("No schedule file specified, so automatic group detection will be disabled.");
            return Collections.emptyMap();
        }

        var schedules = ScheduleLoader.load(scheduleFile);
        if (schedules.isEmpty())
            throw new InvalidScheduleFormatException(scheduleFile + " is empty.");

        return schedules;
    }

}
