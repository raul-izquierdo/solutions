package es.uniovi.raul.solutions.course.naming;

/**
 * An interface to identify solution repositories.
 */
@FunctionalInterface
public interface SolutionIdentifier {
    /**
     * Indicates whether the given repository name corresponds to a solution repository.
     */
    boolean isSolutionRepository(String repository);

}
