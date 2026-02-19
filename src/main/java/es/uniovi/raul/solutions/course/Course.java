package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.util.*;

/**
 * A Course is a layer of abstraction over a GitHub organization. Instead of teams and repositories,
 * a course shows groups and solution repositories. That is, filters teams that correspond to groups and repositories that correspond to solutions.
 *
 * A Course represents an immutable snapshot of groups and solutions.
 *
 * @param groups the groups in the course (teams that correspond to groups)
 * @param solutions the names of all the repositories that correspond to solutions of assignments in the course
 */
public record Course(List<Group> groups, List<String> solutions) {

    public Course {
        notNull(groups, solutions);
        groups = List.copyOf(groups);
        solutions = List.copyOf(solutions);
    }

    public boolean solutionExists(String solution) {
        notNull(solution);

        return solutions.contains(solution);
    }
}
