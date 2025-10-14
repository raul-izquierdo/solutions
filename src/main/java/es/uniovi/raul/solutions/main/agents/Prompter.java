package es.uniovi.raul.solutions.main.agents;

/**
 * Abstraction for user confirmations. Allows swapping console prompts with
 * alternatives and makes callers easy to test with lambdas/fakes.
 */
@FunctionalInterface
public interface Prompter {
    boolean confirm(String message, Object... args);
}
