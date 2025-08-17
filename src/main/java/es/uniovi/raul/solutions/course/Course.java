package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.course.naming.TeamNaming.*;
import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.util.*;

import es.uniovi.raul.solutions.course.naming.SolutionsNaming;
import es.uniovi.raul.solutions.github.GithubConnection;
import es.uniovi.raul.solutions.github.GithubConnection.*;

/**
 * A Course is a layer of abstraction over a GitHub organization. Instead of teams and repositories,
 * a course shows groups and solution repositories.
 */

public final class Course {

    private String organizationName;
    private GithubConnection githubApi;
    private Map<String, Schedule> schedule;

    private List<Group> cachedGroups;
    private List<String> cachedSolutions;

    public Course(String organizationName, GithubConnection githubApi) {
        this(organizationName, githubApi, Collections.emptyMap());
    }

    public Course(String organizationName, GithubConnection githubApi, Map<String, Schedule> schedule) {

        notNull(githubApi, organizationName, schedule);

        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.schedule = schedule;
    }

    /**
     * Returns the groups in the course (teams that correspond to groups).
     */
    public List<Group> getGroups()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        if (cachedGroups == null)
            cachedGroups = downloadGroups();

        return cachedGroups;
    }

    public Optional<Group> findGroup(String name)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return getGroups().stream()
                .filter(group -> group.name().equals(name))
                .findFirst();
    }

    public Group getGroup(String groupName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return findGroup(groupName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group '" + groupName + "' is not a valid group in this course"));
    }

    /**
     * Returns the names of all the repositories that correspond to solutions of assignments in the course.
     */
    public List<String> getSolutions()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        if (cachedSolutions == null)
            cachedSolutions = downloadSolutions();

        return cachedSolutions;
    }

    public void showSolutionToGroup(String solution, String groupName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        notNull(solution, groupName);

        if (!getSolutions().contains(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        githubApi.addTeamToRepository(organizationName, solution, getGroup(groupName).teamSlug());
    }

    public void hideSolutionFromGroup(String solution, String groupName)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        notNull(solution, groupName);

        if (!getSolutions().contains(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        githubApi.removeTeamFromRepository(organizationName, solution, getGroup(groupName).teamSlug());
    }

    //# ------------------------------------------------------------------
    //# Auxiliary methods

    private List<Group> downloadGroups()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi
                .getTeams(organizationName).stream()
                .filter(team -> isGroupTeam(team.displayName()))
                .map(team -> {
                    var group = toGroup(team.displayName());

                    return new Group(
                            group,
                            Optional.ofNullable(schedule.get(group)),
                            team.slug(),
                            this);
                })
                .toList();
    }

    private List<String> downloadSolutions()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        return githubApi.getAllRepositories(organizationName).stream()
                .filter(SolutionsNaming::isSolutionRepository)
                .toList();
    }
}
