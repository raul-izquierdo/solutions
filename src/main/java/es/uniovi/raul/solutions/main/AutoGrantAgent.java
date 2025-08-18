package es.uniovi.raul.solutions.main;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import es.uniovi.raul.solutions.course.Course;
import es.uniovi.raul.solutions.course.Group;
import es.uniovi.raul.solutions.github.GithubConnection.RejectedOperationException;
import es.uniovi.raul.solutions.github.GithubConnection.UnexpectedFormatException;

/**
 * Encapsulates time-based suggestion logic.
 */
final class AutoGrantAgent {
    private final Clock clock;

    AutoGrantAgent(Clock clock) {
        this.clock = clock;
    }

    /**
     * Attempts to automatically grant access to the next solution for the single
     * scheduled group at the current time. Prompts the user for confirmation.
     *
     * @return true if access was granted, false otherwise
     */
    boolean tryAutomaticSelection(Course course, Prompter prompter)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        var guessedGroupOpt = guessGroup(course.getGroups());
        if (guessedGroupOpt.isEmpty())
            return false;

        var guessedSolutionOpt = guessSolution(guessedGroupOpt.get(), course.getAllSolutions());
        if (guessedSolutionOpt.isEmpty())
            return false;

        var guessedGroup = guessedGroupOpt.get();
        var guessedSolution = guessedSolutionOpt.get();

        if (!prompter.confirm(String.format("(now: '%s' %s) Proceed to show '%s' to group '%s'?",
                today(), format(currentTime()), guessedSolution, guessedGroup.name())))
            return false;

        guessedGroup.grantAccess(guessedSolution);

        System.out.println("Access granted.");

        return true;
    }

    Optional<Group> guessGroup(List<Group> groups) {
        var today = today();
        var now = currentTime();
        var matching = groups.stream()
                .filter(g -> g.isScheduledFor(today, now))
                .toList();
        return matching.size() == 1 ? Optional.of(matching.get(0)) : Optional.empty();
    }

    Optional<String> guessSolution(Group group, List<String> allSolutions) {
        return allSolutions.stream()
                .sorted()
                .filter(s -> !group.hasAccessTo(s))
                .findFirst();
    }

    private String today() {
        return LocalDate.now(clock).getDayOfWeek().toString().toLowerCase();
    }

    private LocalTime currentTime() {
        return LocalTime.now(clock);
    }

    private String format(LocalTime time) {
        return time.toString().substring(0, 5);
    }
}
