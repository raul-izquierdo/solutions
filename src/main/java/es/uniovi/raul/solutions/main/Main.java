package es.uniovi.raul.solutions.main;

import java.util.Optional;

import es.uniovi.raul.solutions.cli.*;

import static es.uniovi.raul.solutions.cli.Console.*;

import java.time.Clock;

/**
 * Entry point for the application.
 */
public class Main {

    public static void main(String[] args) {

        Optional<Arguments> argumentsOpt = ArgumentsParser.parse(args);
        if (argumentsOpt.isEmpty()) {
            System.exit(1);
            return;
        }

        int exitCode = 0;
        try {
            var app = new App(Clock.systemDefaultZone(), Console::confirmation);
            exitCode = app.run(argumentsOpt.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            printError("Operation was interrupted.");
            exitCode = 1;

        } catch (Exception e) {
            printError(e.getMessage());
            exitCode = 1;
        }

        System.exit(exitCode);
    }
}
