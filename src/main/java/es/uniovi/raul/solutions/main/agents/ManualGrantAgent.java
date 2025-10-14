package es.uniovi.raul.solutions.main.agents;

import static es.uniovi.raul.solutions.cli.selector.OptionsSelector.*;

import java.io.IOException;
import java.util.ArrayList;

import es.uniovi.raul.solutions.course.Course;
import es.uniovi.raul.solutions.course.Group;
import es.uniovi.raul.solutions.github.GithubApi.GithubApiException;

/**
 * Encapsulates interactive choosing logic for group and solution.
 */
public final class ManualGrantAgent {

    private final Prompter prompter;

    public ManualGrantAgent(Prompter prompter) {
        this.prompter = prompter;
    }

    public void doManualSelection(Course course)
            throws IOException, GithubApiException, InterruptedException {

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

    private String chooseSolution(Course course, Group chosenGroup)
            throws IOException, GithubApiException, InterruptedException {

        System.out.println("Choose the solution:");

        // Pre-load accessible solutions to avoid lambda exception issues
        var solutionsWithAccess = new ArrayList<String>();
        for (var solution : course.getSolutions()) {
            var accessStatus = chosenGroup.hasAccessTo(solution) ? " [accessible]" : " [hidden]";
            solutionsWithAccess.add(solution + accessStatus);
        }

        int selectedSolutionIndex = showOptions(solutionsWithAccess);
        return course.getSolutions().get(selectedSolutionIndex);
    }

    private void confirmAndApply(String verb, AccessAction action, Group group, String solution)
            throws GithubApiException, InterruptedException, IOException {

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
            throws GithubApiException, InterruptedException, IOException;
}
