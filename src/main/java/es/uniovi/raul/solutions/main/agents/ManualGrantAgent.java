package es.uniovi.raul.solutions.main.agents;

import static es.uniovi.raul.solutions.cli.selector.OptionsSelector.*;
import static java.lang.String.*;

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
            confirmAndApply("revoke", Group::revokeAccess, chosenGroup, chosenSolution);
        else
            confirmAndApply("grant", Group::grantAccess, chosenGroup, chosenSolution);
    }

    private Group chooseGroup(Course course) throws IOException {

        System.out.println("Choose the group:");
        int selectedGroupIndex = showOptions(course.getGroups().stream().map(Group::name).toList());
        return course.getGroups().get(selectedGroupIndex);
    }

    private String chooseSolution(Course course, Group chosenGroup)
            throws IOException, GithubApiException, InterruptedException {

        System.out.println("Choose the solution:");

        // One thing is the solution names ("solution1"), another is what the user sees ("solution1 [accessible]")
        var sortedSolutions = course.getSolutions().stream().sorted().toList();
        var userOptions = new ArrayList<String>();

        // Use for loop to avoid lambda exception issues
        for (var solution : sortedSolutions)
            userOptions.add(solution + (chosenGroup.hasAccessTo(solution) ? " [accessible]" : " [hidden]"));

        int selectedSolutionIndex = showOptions(userOptions);
        return sortedSolutions.get(selectedSolutionIndex);
    }

    private void confirmAndApply(String verb, AccessAction action, Group group, String solution)
            throws GithubApiException, InterruptedException, IOException {

        var message = format("%nDo you want to %s group '%s' access to '%s'?", verb.toUpperCase(), group.name(),
                solution);
        if (prompter.confirm(message)) {
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
