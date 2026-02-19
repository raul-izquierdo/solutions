package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.course.naming.TeamNaming.*;
import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.solutions.course.naming.*;
import es.uniovi.raul.solutions.github.*;
import es.uniovi.raul.solutions.github.GithubApi.*;

/**
 * A Course is a layer of abstraction over a GitHub organization. Instead of teams and repositories,
* a course shows groups and solution repositories. That is, filters teams that correspond to groups and repositories that correspond to solutions.
 *
 * A Course downloads all the information on creation. If updated information is required, a new instance of the Course must be created.
 */

public final class Course {

    private final String organizationName;
    private final GithubApi githubApi;
    private final Map<String, Schedule> schedule;
    private final SolutionsDetectionStrategy solutionsDetector;

    private final List<Group> groups;
    private final List<String> solutions;

    public Course(String organizationName, GithubApi githubApi)
            throws GithubApiException, IOException, InterruptedException {

        this(organizationName, githubApi, Collections.emptyMap());
    }

    public Course(String organizationName, GithubApi githubApi, Map<String, Schedule> schedule)
            throws GithubApiException, IOException, InterruptedException {

        this(organizationName, githubApi, schedule, new RegexSolutionDetector(".*solution$"));
    }

    public Course(String organizationName, GithubApi githubApi, Map<String, Schedule> schedule,
            SolutionsDetectionStrategy solutionIdentifier)
            throws GithubApiException, IOException, InterruptedException {

        notNull(githubApi, organizationName, schedule);

        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.schedule = Map.copyOf(schedule);

        this.solutionsDetector = solutionIdentifier;

        this.groups = List.copyOf(fetchGroups());
        this.solutions = List.copyOf(fetchSolutions());

    }

    /**
     * Returns the groups in the course (teams that correspond to groups).
     */
    public List<Group> getGroups() {
        return groups; // unmodifiable snapshot
    }

    /**
     * Returns the names of all the repositories that correspond to solutions of assignments in the course.
     */
    public List<String> getSolutions() {
        return solutions; // unmodifiable snapshot
    }

    public boolean solutionExists(String solution) {
        notNull(solution);

        return getSolutions().contains(solution);
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods

    private List<Group> fetchGroups()
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
                            organizationName, githubApi, solutionsDetector));
        }

        return groupTeams;
    }

    private List<String> fetchSolutions()
            throws GithubApiException, IOException, InterruptedException {

        return githubApi.fetchAllRepositories(organizationName).stream()
                .filter(solutionsDetector::isSolutionRepository)
                .toList();
    }
}
