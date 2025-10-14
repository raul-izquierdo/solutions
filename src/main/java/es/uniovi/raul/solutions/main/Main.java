package es.uniovi.raul.solutions.main;

import static es.uniovi.raul.solutions.cli.Console.*;

import java.io.*;
import java.time.Clock;
import java.util.*;

import es.uniovi.raul.solutions.cli.*;
import es.uniovi.raul.solutions.cli.Console;
import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.course.naming.RegExpIdentifier;
import es.uniovi.raul.solutions.github.GithubApi.*;
import es.uniovi.raul.solutions.github.GithubApiImpl;
import es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args) {

        Optional<Arguments> argumentsOpt = ArgumentsParser.parse(args);
        if (argumentsOpt.isEmpty()) {
            System.exit(1);
            return;
        }

        int exitCode = 0;
        try {

            exitCode = run(argumentsOpt.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            printError("Operation was interrupted.");
            exitCode = 1;
        } catch (Exception e) {
            printError(e.getMessage());
            exitCode = 1;
        }
        System.exit(exitCode);
    }

    static int run(Arguments arguments) throws IOException, InvalidScheduleFormat, GithubApiException,
            InterruptedException {

        var schedule = loadSchedule(arguments.scheduleFile);

        System.out.print("Connecting with Github... ");
        var connection = new GithubApiImpl(arguments.token);
        var course = new Course(arguments.organization, connection, schedule,
                new RegExpIdentifier(arguments.solutionRegex));
        System.out.println("done.\n");

        // If there are no groups or solutions, there's nothing to do. Print an informative message and exit.
        if (course.getGroups().isEmpty()) {
            printWarning("No groups found in the organization. Exiting.");
            return -1;
        }
        if (course.getSolutions().isEmpty()) {
            printWarning("No solutions found in the organization. Exiting.");
            return -1;
        }

        Prompter prompter = Console::confirmation;
        var agent = new AutoGrantAgent(Clock.systemDefaultZone(), prompter);
        if (agent.tryAutomaticSelection(course)) {
            System.out.println("Access granted.");
            return 0;
        }

        var manualAgent = new ManualGrantAgent(prompter);
        manualAgent.doManualSelection(course);

        return 0;
    }

    private static Map<String, Schedule> loadSchedule(String scheduleFile)
            throws IOException, es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat {

        // If scheduleFile is the default "schedule.csv" and doesn't exist, return empty map
        if ("schedule.csv".equals(scheduleFile)) {
            File file = new File(scheduleFile);
            if (!file.exists()) {
                Console.printWarning("Default schedule file '" + scheduleFile
                        + "' was not found. Group detection will be disabled.");
                return Collections.emptyMap();
            }
        }

        System.out.print("\nLoading schedule from '" + scheduleFile + "'... ");

        var schedules = es.uniovi.raul.solutions.schedule.ScheduleLoader.load(scheduleFile);
        if (schedules.isEmpty())
            throw new es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat(
                    scheduleFile + " is empty.");

        System.out.println("done.");

        return schedules;
    }
}
