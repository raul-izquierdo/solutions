package es.uniovi.raul.solutions.course.naming;

import static es.uniovi.raul.solutions.debug.Debug.*;

/**
 * Strategy for naming the repositories that are solutions to assignments.
 */

// There is no need for a proper Strategy Pattern
public final class SolutionsNaming {

    public static boolean isSolutionRepository(String repository) {
        notNull(repository, "repository");

        return repository.endsWith("solution");
    }

}
