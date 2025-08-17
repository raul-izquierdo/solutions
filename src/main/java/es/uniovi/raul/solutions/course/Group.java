package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.util.Optional;

import es.uniovi.raul.solutions.github.GithubConnection.*;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String name;
    private final Optional<Schedule> schedule;
    private final String teamSlug;
    private Course course;

    Group(String name, Optional<Schedule> schedule, String teamSlug, Course course) {
        notNull(name, teamSlug, schedule, course);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.course = course;
    }

    public String name() {
        return name;
    }

    public Optional<Schedule> schedule() {
        return schedule;
    }

    public void showSolution(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        course.showSolutionToGroup(solution, name);
    }

    public void hideSolution(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        course.hideSolutionFromGroup(solution, name);
    }

    // Don't make the slug public. It could be used by mistake instead of the name of the group
    String teamSlug() {
        return teamSlug;
    }
}
