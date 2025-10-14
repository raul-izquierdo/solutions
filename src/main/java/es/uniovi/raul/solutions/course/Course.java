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

    private String organizationName;
    private GithubApi githubApi;
    private Map<String, Schedule> schedule;
    private SolutionIdentifier solutionIdentifier;

    private List<Group> groups;
    private List<String> solutions;

    public Course(String organizationName, GithubApi githubApi)
            throws GithubApiException, IOException, InterruptedException {

        this(organizationName, githubApi, Collections.emptyMap());
    }

    public Course(String organizationName, GithubApi githubApi, Map<String, Schedule> schedule)
            throws GithubApiException, IOException, InterruptedException {

        this(organizationName, githubApi, schedule, new RegExpIdentifier(".*solution$"));
    }

    public Course(String organizationName, GithubApi githubApi, Map<String, Schedule> schedule,
            SolutionIdentifier solutionIdentifier)
            throws GithubApiException, IOException, InterruptedException {

        notNull(githubApi, organizationName, schedule);

        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.schedule = schedule;

        this.solutionIdentifier = solutionIdentifier;

        this.groups = fetchGroups();
        this.solutions = fetchSolutions();

    }

    public GithubApi githubConnection() {
        return githubApi;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public SolutionIdentifier getSolutionIdentifier() {
        return solutionIdentifier;
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

    public void grantAccess(Group group, String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(group, solution);

        if (!solutionExists(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        githubApi.grantAccess(organizationName, solution, group.getSlug());
    }

    public void revokeAccess(Group group, String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(group, solution);

        if (!solutionExists(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        githubApi.revokeAccess(organizationName, solution, group.getSlug());
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
                            organizationName, githubApi, solutionIdentifier));
        }

        return groupTeams;
    }

    private List<String> fetchSolutions()
            throws GithubApiException, IOException, InterruptedException {

        return githubApi.fetchAllRepositories(organizationName).stream()
                .filter(solutionIdentifier::isSolutionRepository)
                .toList();
    }
}
