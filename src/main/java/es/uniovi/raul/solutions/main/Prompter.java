package es.uniovi.raul.solutions.main;

/**
 * Abstraction for user confirmations. Allows swapping console prompts with
 * alternatives and makes callers easy to test with lambdas/fakes.
 */
@FunctionalInterface
interface Prompter {
    boolean confirm(String message, Object... args);
}
