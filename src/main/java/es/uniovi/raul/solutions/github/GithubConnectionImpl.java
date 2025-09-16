package es.uniovi.raul.solutions.github;

import static java.net.http.HttpRequest.BodyPublishers.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.Builder;
import java.util.*;

import com.fasterxml.jackson.databind.*;

/**
 * Github API implementation.
 */
public final class GithubConnectionImpl implements GithubConnection {

    private String token;

    public GithubConnectionImpl(String token) {
        if (token == null || token.isBlank())
            throw new IllegalArgumentException("Token cannot be null or blank.");

        this.token = token;
    }

    @Override
    public List<Team> fetchTeams(String organization)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        List<Team> teams = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = "https://api.github.com/orgs/" + organization + "/teams";
            HttpRequest request = createHttpRequestBuilder(url).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RejectedOperationException("Failed to get existing teams for organization '" + organization
                        + "'. Status: " + response.statusCode() + ". Response: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException("Expected a JSON array for teams, got: " + root.getNodeType());

            for (JsonNode node : root) {
                JsonNode nameNode = node.get("name");
                JsonNode slugNode = node.get("slug");
                if (nameNode == null || !nameNode.isTextual() || slugNode == null || !slugNode.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'name' and 'slug' fields of type string in each team object, got: "
                                    + node.toString());

                teams.add(new Team(nameNode.asText(), slugNode.asText()));
            }
            return teams;
        }
    }

    @Override
    public List<String> fetchAllRepositories(String organization)
            throws IOException, InterruptedException, RejectedOperationException, UnexpectedFormatException {

        List<String> repositories = new ArrayList<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/repos", organization);
            HttpRequest request = createHttpRequestBuilder(url).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RejectedOperationException("Failed to get repositories for organization '" + organization
                        + "'. Status: " + response.statusCode() + ". Response: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException(
                        "Expected a JSON array for repositories, got: " + root.getNodeType());

            for (JsonNode node : root) {
                JsonNode nameNode = node.get("name");
                if (nameNode == null || !nameNode.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'name' field of type string in each repository object, got: " + node.toString());

                repositories.add(nameNode.asText());
            }
            return repositories;
        }
    }

    @Override
    public List<String> fetchRepositoriesForTeam(String organization, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/teams/%s/repos", organization, teamSlug);
            HttpRequest request = createHttpRequestBuilder(url).build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
                throw new RejectedOperationException("Failed to get repositories for team '" + teamSlug
                        + "' in organization '" + organization + "'. Status: "
                        + response.statusCode() + ". Response: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            if (!root.isArray())
                throw new UnexpectedFormatException(
                        "Expected a JSON array for the team's repositories, got: " + root.getNodeType());

            List<String> repositories = new ArrayList<>();
            for (JsonNode node : root) {
                JsonNode fullNameNode = node.get("full_name");
                if (fullNameNode == null || !fullNameNode.isTextual())
                    throw new UnexpectedFormatException(
                            "Expected 'full_name' field of type string in each repository object, got: "
                                    + node.toString());

                repositories.add(fullNameNode.asText());
            }
            return repositories;
        }
    }

    @Override
    public void grantAccess(String organization, String repository, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            String url = String.format("https://api.github.com/orgs/%s/teams/%s/repos/%s/%s",
                    organization, teamSlug, organization, repository);
            HttpRequest request = createHttpRequestBuilder(url)
                    .header("Content-Type", "application/json")
                    .PUT(ofString("{\"permission\":\"pull\"}")) // Grant read-only permission to the team on the repository
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204)
                throw new RejectedOperationException("Failed to add team to repository in organization '" + organization
                        + "'. Status: " + response.statusCode() + ". Response: " + response.body());

        }
    }

    @Override
    public void revokeAccess(String organization, String repository, String teamSlug)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {
            String url = String.format("https://api.github.com/orgs/%s/teams/%s/repos/%s/%s",
                    organization, teamSlug, organization, repository);
            HttpRequest request = createHttpRequestBuilder(url)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204)
                throw new RejectedOperationException("Failed to remove team from repository in organization '"
                        + organization + "'. Status: " + response.statusCode() + ". Response: " + response.body());

        }
    }

    //# Auxiliary methods -----------------------------------

    private Builder createHttpRequestBuilder(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json");
    }

}
