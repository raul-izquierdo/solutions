package es.uniovi.raul.solutions.main;

import static es.uniovi.raul.solutions.cli.Console.*;
import static es.uniovi.raul.solutions.course.naming.TeamNaming.*;

import java.io.*;
import java.time.Clock;
import java.util.*;

import es.uniovi.raul.solutions.cli.*;
import es.uniovi.raul.solutions.cli.Console;
import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.course.naming.*;
import es.uniovi.raul.solutions.github.*;
import es.uniovi.raul.solutions.github.GithubApi.GithubApiException;
import es.uniovi.raul.solutions.main.agents.*;
import es.uniovi.raul.solutions.schedule.ScheduleLoader;
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

        final var schedule = loadSchedule(arguments.scheduleFile);

        System.out.print("Connecting with Github... ");
        GithubApi connection = new GithubApiImpl(arguments.token);
        if (arguments.dryRun) {
            connection = new DryRunGithubApi(connection);
            System.out.println("=== DRY RUN MODE - No changes will be made ===\n");
        }
        System.out.println("done.");

        System.out.print("Fetching groups and solutions... ");
        var course = createCourse(arguments, schedule, connection);
        System.out.println("done.\n");

        // If there are no groups or solutions, there's nothing to do. Print an informative message and exit.
        if (course.groups().isEmpty()) {
            printWarning("No groups found in the organization. Exiting.");
            return 2;
        }
        if (course.solutions().isEmpty()) {
            printWarning("No solutions found in the organization. Exiting.");
            return 2;
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

    private static Course createCourse(Arguments arguments, final Map<String, Schedule> schedule, GithubApi connection)
            throws GithubApiException, IOException, InterruptedException {

        var solutionsDetector = new RegexSolutionDetector(arguments.solutionRegex);
        var groups = fetchGroups(arguments.organization, connection, schedule, solutionsDetector);
        var solutions = fetchSolutions(arguments.organization, connection, solutionsDetector);
        return new Course(groups, solutions);
    }

    private static Map<String, Schedule> loadSchedule(String scheduleFile)
            throws IOException, ScheduleLoader.InvalidScheduleFormat {

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

        var schedules = ScheduleLoader.load(scheduleFile);
        if (schedules.isEmpty())
            throw new ScheduleLoader.InvalidScheduleFormat(scheduleFile + " is empty.");

        System.out.println("done.");

        return schedules;
    }

    private static List<Group> fetchGroups(String organizationName, GithubApi githubApi,
            Map<String, Schedule> schedule, SolutionsDetectionStrategy solutionsDetector)
            throws GithubApiException, IOException, InterruptedException {

        List<Team> filteredTeams = githubApi
                .fetchTeams(organizationName).stream()
                .filter(team -> isGroupTeam(team.displayName()))
                .toList();

        List<Group> groupTeams = new ArrayList<>();
        for (var team : filteredTeams) {
            var group = toGroup(team.displayName());
            groupTeams.add(
                    new Group(group, team.slug(), Optional.ofNullable(schedule.get(group)),
                            githubApi, organizationName, solutionsDetector));
        }

        return groupTeams;
    }

    private static List<String> fetchSolutions(String organizationName, GithubApi githubApi,
            SolutionsDetectionStrategy solutionsDetector)
            throws GithubApiException, IOException, InterruptedException {

        return githubApi.fetchAllRepositories(organizationName).stream()
                .filter(solutionsDetector::isSolutionRepository)
                .toList();
    }
}
