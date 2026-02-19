package es.uniovi.raul.solutions.course.naming;

import java.util.regex.Pattern;

/**
 * Identifies solution repositories based on a regular expression.
 */
public final class RegexSolutionDetector implements SolutionsDetectionStrategy {
    private final Pattern pattern;

    public RegexSolutionDetector(String expression) {
        this.pattern = Pattern.compile(expression);
    }

    @Override
    public boolean isSolutionRepository(String repository) {
        return pattern.matcher(repository).matches();
    }

}
