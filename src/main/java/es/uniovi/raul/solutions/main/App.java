package es.uniovi.raul.solutions.main;

import java.io.IOException;
import java.time.Clock;
import java.util.Collections;
import java.util.Map;

import es.uniovi.raul.solutions.cli.Arguments;
import es.uniovi.raul.solutions.cli.Console;
import es.uniovi.raul.solutions.course.Course;
import es.uniovi.raul.solutions.course.Schedule;
import es.uniovi.raul.solutions.github.GithubConnection.RejectedOperationException;
import es.uniovi.raul.solutions.github.GithubConnection.UnexpectedFormatException;
import es.uniovi.raul.solutions.github.GithubConnectionImpl;
import es.uniovi.raul.solutions.schedule.ScheduleLoader;
import es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat;

/**
 * Orchestrates the CLI flow.
 */
final class App {

    private final Clock clock;
    private final Prompter prompter;

    App(Clock clock, Prompter prompter) {
        this.clock = clock;
        this.prompter = prompter;
    }

    int run(Arguments arguments) throws IOException,
            InterruptedException,
            InvalidScheduleFormat,
            UnexpectedFormatException,
            RejectedOperationException {

        var schedule = loadSchedule(arguments.scheduleFile);

        System.out.print("Connecting with Github... ");
        var connection = new GithubConnectionImpl(arguments.token);
        var course = new Course(arguments.organization, connection, schedule);
        System.out.println("done.\n");

        var suggester = new AutoGrantAgent(clock, prompter);
        if (suggester.tryAutomaticSelection(course))
            return 0;

        var manualAgent = new ManualGrantAgent(prompter);
        manualAgent.doManualSelection(course);

        return 0;
    }

    private Map<String, Schedule> loadSchedule(String scheduleFile)
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
}
