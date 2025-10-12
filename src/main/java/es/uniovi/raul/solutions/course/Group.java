package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import es.uniovi.raul.solutions.github.GithubApi.*;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String name;
    private final Optional<Schedule> schedule;
    private final String teamSlug;
    private Course course;

    private List<String> accesibleSolutions;

    Group(String name, Optional<Schedule> schedule, String teamSlug, Course course)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        notNull(name, teamSlug, schedule, course);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;
        this.course = course;

        this.accesibleSolutions = course.fetchGroupSolutions(this);
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

    public List<String> getAccesibleSolutions() {
        return accesibleSolutions;
    }

    public boolean hasAccessTo(String solution) {
        notNull(solution);

        return accesibleSolutions.contains(solution);
    }

    public void grantAccess(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        course.grantAccess(this, solution);
    }

    public void revokeAccess(String solution)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        course.revokeAccess(this, solution);
    }

}
