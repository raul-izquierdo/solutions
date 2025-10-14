package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import es.uniovi.raul.solutions.course.naming.SolutionIdentifier;
import es.uniovi.raul.solutions.github.GithubApi;
import es.uniovi.raul.solutions.github.GithubApi.GithubApiException;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String name;
    private final String teamSlug;
    private final Optional<Schedule> schedule;

    // Lazy loading fields
    private final String organizationName;
    private final GithubApi githubApi;
    private final SolutionIdentifier solutionIdentifier;
    private List<String> accessibleSolutions; // null = not loaded yet

    Group(String name, String teamSlug, Optional<Schedule> schedule,
            String organizationName, GithubApi githubApi, SolutionIdentifier solutionIdentifier) {
        notNull(name, teamSlug, schedule, organizationName, githubApi, solutionIdentifier);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.organizationName = organizationName;
        this.githubApi = githubApi;
        this.solutionIdentifier = solutionIdentifier;
        this.accessibleSolutions = null; // Lazy loading
    }

    public String name() {
        return name;
    }

    public Optional<Schedule> schedule() {
        return schedule;
    }

    public String getSlug() {
        return teamSlug;
    }

    public boolean isScheduledFor(String day, LocalTime time) {
        notNull(day, time);

        return schedule.map(groupSchedule -> groupSchedule.includes(day, time)).orElse(false);
    }

    public List<String> getAccesibleSolutions()
            throws GithubApiException, IOException, InterruptedException {
        return ensureAccessibleSolutions();
    }

    public boolean hasAccessTo(String solution)
            throws GithubApiException, IOException, InterruptedException {
        notNull(solution);

        return ensureAccessibleSolutions().contains(solution);
    }

    public void grantAccess(String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(solution);

        githubApi.grantAccess(organizationName, solution, teamSlug);

        // Invalidate cache since access has changed
        accessibleSolutions = null;
    }

    public void revokeAccess(String solution)
            throws GithubApiException, IOException, InterruptedException {

        notNull(solution);

        githubApi.revokeAccess(organizationName, solution, teamSlug);

        // Invalidate cache since access has changed
        accessibleSolutions = null;
    }

    private List<String> ensureAccessibleSolutions()
            throws GithubApiException, IOException, InterruptedException {
        if (accessibleSolutions == null) {
            accessibleSolutions = fetchGroupSolutions();
        }
        return accessibleSolutions;
    }

    private List<String> fetchGroupSolutions()
            throws GithubApiException, IOException, InterruptedException {
        return githubApi
                .fetchRepositoriesForTeam(organizationName, teamSlug)
                .stream()
                .filter(solutionIdentifier::isSolutionRepository)
                // Returned repos have the format "<org>/<repo>". We only want the repo name.
                .map(solution -> (solution.contains("/"))
                        ? solution.substring(solution.lastIndexOf('/') + 1)
                        : solution)
                .toList();
    }

}
