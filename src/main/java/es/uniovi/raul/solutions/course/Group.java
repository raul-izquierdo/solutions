package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.time.LocalTime;
import java.util.*;

/**
 * Represents a group of the course and its associated github team.
 */
public final class Group {

    private final String name;
    private final String teamSlug;
    private List<String> accesibleSolutions;
    private final Optional<Schedule> schedule;

    Group(String name, String teamSlug, List<String> accessibleSolutions, Optional<Schedule> schedule) {
        notNull(name, teamSlug, schedule, accessibleSolutions);

        this.name = name;
        this.teamSlug = teamSlug;
        this.schedule = schedule;

        this.accesibleSolutions = new ArrayList<>(accessibleSolutions);
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

}
