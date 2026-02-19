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

    private final String name;
    private final String teamSlug;
    private final Optional<Schedule> schedule;
    private final String organizationName;
    private final GithubApi githubApi;
    private final SolutionsDetectionStrategy solutionIdentifier;

    // Accesible solutions -> solution repositories that the group has access to. This is a subset of the solutions in the course. Lazily loaded and cached.
    private List<String> accesibleSolutions; // null = not loaded yet

    public Group(String name, String teamSlug, Optional<Schedule> schedule,
            String organizationName, GithubApi githubApi, SolutionsDetectionStrategy solutionIdentifier) {
        notNull(name, teamSlug, schedule, organizationName, githubApi, solutionIdentifier);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.solutionIdentifier = solutionIdentifier;
        this.accesibleSolutions = null; // Lazy loading
    }

    public String name() {
        return name;
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
        if (accesibleSolutions == null) {
            accesibleSolutions = fetchAccesibleSolutions();
        }
        return Collections.unmodifiableList(accesibleSolutions);
    }

    // Fetches the list of solution repositories that the group has access to.
    private List<String> fetchAccesibleSolutions()
            throws GithubApiException, IOException, InterruptedException {

        return githubApi
                .fetchRepositoriesForTeam(organizationName, teamSlug)
                .stream()
                .filter(solutionIdentifier::isSolutionRepository)
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
