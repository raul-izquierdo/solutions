package es.uniovi.raul.solutions.cli;

import static java.lang.String.*;

import java.util.Scanner;

public final class Console {

    public static void printError(String message) {
        System.err.println(format("%n[Error] %s%n", message));
    }

    public static void printWarning(String message) {
        System.out.println(format("%n[Warning] %s%n", message));
    }

    public static boolean confirmation(String message) {
        System.out.print(format("%s (y/N): ", message));
        @SuppressWarnings("resource")
        String response = new Scanner(System.in).nextLine().trim().toLowerCase();
        // Don't close the scanner, because showOptions will fail (System.in will be closed)
        return "y".equals(response);
    }

    public static boolean confirmation(String message, Object... args) {
        return confirmation(String.format(message, args));
    }
}
