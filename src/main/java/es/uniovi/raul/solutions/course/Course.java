package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.util.*;

/**
 * A Course is a layer of abstraction over a GitHub organization. Instead of teams and repositories,
 * a course shows groups and solution repositories. That is, filters teams that correspond to groups and repositories that correspond to solutions.
 *
 * A Course represents an immutable snapshot of groups and solutions.
 */

public final class Course {

    private final String organizationName;
    private final Map<String, Schedule> schedule;
    private final List<Group> groups;
    private final List<String> solutions;

    public Course(String organizationName, Map<String, Schedule> schedule,
            List<Group> groups, List<String> solutions) {

        notNull(organizationName, schedule, groups, solutions);

        this.organizationName = organizationName;
        this.schedule = Map.copyOf(schedule);
        this.groups = List.copyOf(groups);
        this.solutions = List.copyOf(solutions);
    }

    /**
     * Returns the groups in the course (teams that correspond to groups).
     */
    public List<Group> getGroups() {
        return groups; // unmodifiable snapshot
    }

    /**
     * Returns the names of all the repositories that correspond to solutions of assignments in the course.
     */
    public List<String> getSolutions() {
        return solutions; // unmodifiable snapshot
    }

    public boolean solutionExists(String solution) {
        notNull(solution);

        return getSolutions().contains(solution);
    }
}
