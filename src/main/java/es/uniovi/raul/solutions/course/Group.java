package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import es.uniovi.raul.solutions.github.GithubConnection.*;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String name;
    private final Optional<Schedule> schedule;
    private final String teamSlug;
    private Course course;

    private List<String> groupSolutions;

    Group(String name, Optional<Schedule> schedule, String teamSlug, Course course)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        notNull(name, teamSlug, schedule, course);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.course = course;

        this.groupSolutions = course.githubConnection().getRepositoriesForTeam(course.getName(), teamSlug);
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

    public List<String> getAccesibleSolutions() {
        return groupSolutions;
    }

    public boolean isAccesible(String solution) {
        notNull(solution);

        return groupSolutions.contains(solution);
    }

    public void grantAccess(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        notNull(solution);

        if (!course.solutionExists(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        course.githubConnection().grantAccess(course.getName(), solution, teamSlug);
    }

    public void revokeAccess(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        notNull(solution);

        if (!course.solutionExists(solution))
            throw new IllegalArgumentException("Solution '" + solution + "' is not a valid solution in this course");

        course.githubConnection().revokeAccess(course.getName(), solution, teamSlug);
    }

    // Don't make the slug public. It could be used by mistake instead of the name of the group
    String teamSlug() {
        return teamSlug;
    }
}
