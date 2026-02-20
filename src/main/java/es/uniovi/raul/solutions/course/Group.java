package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import es.uniovi.raul.solutions.course.naming.SolutionsDetectionStrategy;
import es.uniovi.raul.solutions.github.GithubApi;
import es.uniovi.raul.solutions.github.GithubApi.GithubApiException;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String groupName;
    private final String teamSlug;
    private final Optional<Schedule> schedule;
    private final String organizationName;
    private final GithubApi githubApi;
    private final SolutionsDetectionStrategy solutionDetectionStrategy;

    // Accesible solutions -> solution repositories that the group has access to. This is a subset of the solutions in the course. Lazily loaded and cached.
    private List<String> accesibleSolutions; // null = not loaded yet

    /**
     * Constructs a Group with the specified configuration for managing a GitHub classroom group.
     *
     * @param groupName the name that is shown to the user, must not be null
     * @param teamSlug the ID for the team on GitHub, must not be null
     * @param schedule an optional schedule for the group, must not be null
     * @param githubApi the GitHub API wrapper for interacting with GitHub services, must not be null
     * @param organizationName the name of the GitHub organization where the solutions are hosted, must not be null
     * @param solutionDetectionStrategy the strategy used to detect solutions, must not be null
     * @throws IllegalArgumentException if any parameter is null
     */
    public Group(String groupName, String teamSlug, Optional<Schedule> schedule,
            GithubApi githubApi, String organizationName, SolutionsDetectionStrategy solutionDetectionStrategy) {
        notNull(groupName, teamSlug, schedule, organizationName, githubApi, solutionDetectionStrategy);

        this.groupName = groupName;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.solutionDetectionStrategy = solutionDetectionStrategy;
        this.accesibleSolutions = null; // Lazy loading
    }

    public String name() {
        return groupName;
    }

    public Optional<Schedule> schedule() {
        return schedule;
    }

    public boolean isScheduledFor(String day, LocalTime time) {
        notNull(day, time);

        return schedule.map(groupSchedule -> groupSchedule.includes(day, time)).orElse(false);
    }

    /**
     * Returns the list of solution repositories that the group has access to. This is a subset of the solutions in the course.
     */
    public List<String> getAccesibleSolutions()
            throws GithubApiException, IOException, InterruptedException {

        return fetchSolutionsIfNeeded();
    }

    public boolean hasAccessTo(String solution)
            throws GithubApiException, IOException, InterruptedException {
        notNull(solution);

        return fetchSolutionsIfNeeded().contains(solution);
    }

    public void grantAccess(String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(solution);

        githubApi.grantAccess(organizationName, solution, teamSlug);

        // Invalidate cache since access has changed
        accesibleSolutions = null;
    }

    public void revokeAccess(String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(solution);

        githubApi.revokeAccess(organizationName, solution, teamSlug);

        // Invalidate cache since access has changed
        accesibleSolutions = null;
    }

    // Lazy loading of the solution repositories that the group has access to.
    private List<String> fetchSolutionsIfNeeded()
            throws GithubApiException, IOException, InterruptedException {

        if (accesibleSolutions == null)
            accesibleSolutions = fetchAccesibleSolutions();

        return Collections.unmodifiableList(accesibleSolutions);
    }

    // Fetches the list of solution repositories that the group has access to.
    private List<String> fetchAccesibleSolutions()
            throws GithubApiException, IOException, InterruptedException {

        return githubApi
                .fetchRepositoriesForTeam(organizationName, teamSlug)
                .stream()
                .filter(solutionDetectionStrategy::isSolutionRepository)
                .map(this::extractRepositoryName)
                .toList();
    }

    /**
     * Extracts the repository name from a full name that may include organization prefix.
     * For example:
     * "org/repo" -> "repo"
     * "repo" -> "repo"
     */
    private String extractRepositoryName(String fullName) {
        int lastSlash = fullName.lastIndexOf('/');
        return lastSlash >= 0 ? fullName.substring(lastSlash + 1) : fullName;
    }

}
