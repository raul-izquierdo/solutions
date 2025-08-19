package es.uniovi.raul.solutions.github;

import java.io.IOException;
import java.util.*;

/**
 * Interface for interacting with the GitHub API to manage teams and their members within an organization.
 */
public interface GithubConnection {

    /**
     * Downloads the list of teams from the specified organization.
     *
     * @param organization Organization name
     * @return List of teams in the organization
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     */
    List<Team> fetchTeams(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Downloads the list of repositories from the specified organization.
     *
     * @param organization Organization name
     * @return List of repositories in the organization
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws UnexpectedFormatException if the response format is unexpected
     * @throws IOException if a network error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    List<String> fetchAllRepositories(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
    * Returns the list of repositories in which the team is a member.
    *
    * @param organization Organization name
    * @param teamSlug     Slug of the team
    * @return List of repositories for the team
    * @throws UnexpectedFormatException if the response format is unexpected
    * @throws RejectedOperationException if the operation is rejected by GitHub API
    * @throws IOException if a network error occurs
    * @throws InterruptedException if the operation is interrupted
    */
    List<String> fetchRepositoriesForTeam(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Adds a team to a repository in the specified organization.
     *
     * @param organization Organization name
     * @param repository   Name of the repository
     * @param teamSlug     Slug of the team to add
     * @throws UnexpectedFormatException if the response format is unexpected
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws IOException if a network error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    void grantAccess(String organization, String repository, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
     * Removes a team from a repository in the specified organization.
     *
     * @param organization Organization name
     * @param repository   Name of the repository
     * @param teamSlug     Slug of the team to remove
     * @throws UnexpectedFormatException if the response format is unexpected
     * @throws RejectedOperationException if the operation is rejected by GitHub API
     * @throws IOException if a network error occurs
     * @throws InterruptedException if the operation is interrupted
     */
    void revokeAccess(String organization, String repository, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException;

    /**
    * Exception thrown when the response format is unexpected. Probably the format has changed and this code needs to be updated.
    */
    class UnexpectedFormatException extends Exception {
        public UnexpectedFormatException(String message) {
            super(message);
        }
    }

    /**
    * Exception thrown when the operation is rejected by GitHub API.
    */
    class RejectedOperationException extends Exception {
        public RejectedOperationException(String message) {
            super(message);
        }
    }
}
