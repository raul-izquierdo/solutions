package es.uniovi.raul.solutions.main;

import static es.uniovi.raul.solutions.cli.options.OptionsSelector.showOptions;

import java.io.IOException;

import es.uniovi.raul.solutions.course.Course;
import es.uniovi.raul.solutions.course.Group;
import es.uniovi.raul.solutions.github.GithubConnection.RejectedOperationException;
import es.uniovi.raul.solutions.github.GithubConnection.UnexpectedFormatException;

/**
 * Encapsulates interactive choosing logic for group and solution.
 */
public final class ManualGrantAgent {

    private Prompter prompter;

    public ManualGrantAgent(Prompter prompter) {
        this.prompter = prompter;
    }

    public void doManualSelection(Course course)
            throws IOException, UnexpectedFormatException, RejectedOperationException, InterruptedException {

        var chosenGroup = chooseGroup(course);

        var chosenSolution = chooseSolution(course, chosenGroup);

        if (chosenGroup.hasAccessTo(chosenSolution))
            confirmAndApply("Revoke", Group::revokeAccess, chosenGroup, chosenSolution);
        else
            confirmAndApply("Grant", Group::grantAccess, chosenGroup, chosenSolution);
    }

    private Group chooseGroup(Course course) throws IOException {

        System.out.println("Choose the group:");
        int selectedGroupIndex = showOptions(course.getGroups().stream().map(Group::name).toList());
        return course.getGroups().get(selectedGroupIndex);
    }

    private String chooseSolution(Course course, Group chosenGroup) throws IOException {

        System.out.println("Choose the solution:");
        int selectedSolutionIndex = showOptions(course.getAllSolutions()
                .stream()
                .map(solution -> solution + (chosenGroup.hasAccessTo(solution) ? " [accessible]" : " [hidden]"))
                .toList());

        return course.getAllSolutions().get(selectedSolutionIndex);
    }

    private void confirmAndApply(String verb, AccessAction action, Group group, String solution)
            throws UnexpectedFormatException, RejectedOperationException, InterruptedException, IOException {

        if (prompter.confirm(verb + " access?")) {
            action.apply(group, solution);
            System.out.println("Access " + verb.toLowerCase() + "ed."); // Very hacky and cutre
        } else
            System.out.println("Operation cancelled.");
    }
}

@FunctionalInterface
interface AccessAction {
    void apply(Group group, String solution)
            throws UnexpectedFormatException, RejectedOperationException, InterruptedException, IOException;
}
