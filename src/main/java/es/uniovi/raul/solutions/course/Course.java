package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.course.naming.TeamNaming.*;
import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletionException;

import es.uniovi.raul.solutions.course.naming.SolutionsNaming;
import es.uniovi.raul.solutions.github.GithubConnection;
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

        this.groups = downloadGroups();
        this.solutions = downloadSolutions();

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
    public List<String> getAllSolutions() {
        return solutions;
    }

    public boolean solutionExists(String solution) {

        notNull(solution);

        return getAllSolutions().stream()
                .anyMatch(sol -> sol.equals(solution));
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods

    private List<Group> downloadGroups()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var solutionRepos = githubApi
                .getTeams(organizationName).stream()
                .filter(team -> isGroupTeam(team.displayName()))
                .toList();

        List<Group> groupList = new ArrayList<>();
        for (var team : solutionRepos) {
            var group = toGroup(team.displayName());
            groupList.add(new Group(group, Optional.ofNullable(schedule.get(group)), team.slug(), this));
        }
        return groupList;
    }

    private List<String> downloadSolutions()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi.getAllRepositories(organizationName).stream()
                .filter(SolutionsNaming::isSolutionRepository)
                .toList();
    }
}
