package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.course.naming.TeamNaming.*;
import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.solutions.course.naming.SolutionsNaming;
import es.uniovi.raul.solutions.github.*;
import es.uniovi.raul.solutions.github.GithubConnection.*;

/**
 * A Course is a layer of abstraction over a GitHub organization. Instead of teams and repositories,
 * a course shows groups and solution repositories.
 *
 * A Course downloads all the information on creation. If updated information is required, a new
 * instance of the Course must be created.
 */

public final class Course {

    private String organizationName;
    private GithubConnection githubApi;
    private Map<String, Schedule> schedule;

    private List<Group> groups;
    private List<String> solutions;

    public Course(String organizationName, GithubConnection githubApi)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        this(organizationName, githubApi, Collections.emptyMap());
    }

    public Course(String organizationName, GithubConnection githubApi, Map<String, Schedule> schedule)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        notNull(githubApi, organizationName, schedule);

        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.schedule = schedule;

        this.groups = fetchGroups();
        this.solutions = fetchSolutions();
    }

    public GithubConnection githubConnection() {
        return githubApi;
    }

    public String getName() {
        return organizationName;
    }

    /**
     * Returns the groups in the course (teams that correspond to groups).
     */
    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Returns the names of all the repositories that correspond to solutions of assignments in the course.
     */
    public List<String> getSolutions() {
        return solutions;
    }

    public boolean solutionExists(String solution) {

        notNull(solution);

        return getSolutions().contains(solution);
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods

    private List<Group> fetchGroups()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> filteredTeams = githubApi
                .fetchTeams(organizationName).stream()
                .filter(team -> isGroupTeam(team.displayName()))
                .toList();

        List<Group> groupTeams = new ArrayList<>();
        for (var team : filteredTeams) {
            var group = toGroup(team.displayName());
            groupTeams.add(new Group(group, Optional.ofNullable(schedule.get(group)), team.slug(), this));
        }

        return groupTeams;
    }

    private List<String> fetchSolutions()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi.fetchAllRepositories(organizationName).stream()
                .filter(SolutionsNaming::isSolutionRepository)
                .toList();
    }
}
