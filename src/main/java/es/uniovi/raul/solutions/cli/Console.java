package es.uniovi.raul.solutions.cli;

import static java.lang.String.*;

public final class Console {

    public static void printError(String message) {
        System.err.println(format("%n[Error] %s%n", message));
    }

    public static void printWarning(String message) {
        System.out.println(format("%n[Warning] %s%n", message));
    }

}
