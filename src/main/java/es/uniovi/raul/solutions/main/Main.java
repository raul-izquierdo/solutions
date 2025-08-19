package es.uniovi.raul.solutions.main;

import static es.uniovi.raul.solutions.cli.Console.*;

import java.io.IOException;
import java.time.Clock;
import java.util.*;

import es.uniovi.raul.solutions.cli.*;
import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.github.GithubConnection.*;
import es.uniovi.raul.solutions.github.GithubConnectionImpl;
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

    static int run(Arguments arguments) throws IOException, InvalidScheduleFormat, UnexpectedFormatException,
            RejectedOperationException, InterruptedException {

        var schedule = loadSchedule(arguments.scheduleFile);

        System.out.print("Connecting with Github... ");
        var connection = new GithubConnectionImpl(arguments.token);
        var course = new Course(arguments.organization, connection, schedule);
        System.out.println("done.\n");

        Prompter prompter = Console::confirmation;
        var suggester = new AutoGrantAgent(Clock.systemDefaultZone(), prompter);
        if (suggester.tryAutomaticSelection(course))
            return 0;

        var manualAgent = new ManualGrantAgent(prompter);
        manualAgent.doManualSelection(course);

        return 0;
    }

    private static Map<String, Schedule> loadSchedule(String scheduleFile)
            throws IOException, es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat {

        if (scheduleFile == null) {
            Console.printWarning("No schedule file specified, so automatic group detection will be disabled.");
            return Collections.emptyMap();
        }

        var schedules = es.uniovi.raul.solutions.schedule.ScheduleLoader.load(scheduleFile);
        if (schedules.isEmpty())
            throw new es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat(
                    scheduleFile + " is empty.");

        return schedules;
    }
}
