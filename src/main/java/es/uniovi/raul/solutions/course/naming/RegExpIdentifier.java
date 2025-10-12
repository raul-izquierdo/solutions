package es.uniovi.raul.solutions.course.naming;

import java.util.regex.Pattern;

public final class RegExpIdentifier implements SolutionIdentifier {
    private Pattern pattern;

    public RegExpIdentifier(String expression) {
        this.pattern = Pattern.compile(expression);
    }

    @Override
    public boolean isSolutionRepository(String repository) {
        return pattern.matcher(repository).matches();
    }

}
