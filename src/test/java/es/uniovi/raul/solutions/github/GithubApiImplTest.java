package es.uniovi.raul.solutions.github;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GithubApiImpl} error message handling.
 */
class GithubApiImplTest {

    private GithubApiImpl api;

    @BeforeEach
    void setUp() {
        api = new GithubApiImpl("test-token");
    }

    //# Tests for parseGithubErrorMessage -------------------------------

    @Test
    @DisplayName("parseGithubErrorMessage extracts message from valid JSON")
    void parseGithubErrorMessage_validJson() {
        String json = "{\"message\":\"Not Found\",\"documentation_url\":\"https://docs.github.com\"}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertTrue(result.isPresent());
        assertEquals("Not Found", result.get());
    }

    @Test
    @DisplayName("parseGithubErrorMessage handles simple message")
    void parseGithubErrorMessage_simpleMessage() {
        String json = "{\"message\":\"Bad credentials\"}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertTrue(result.isPresent());
        assertEquals("Bad credentials", result.get());
    }

    @Test
    @DisplayName("parseGithubErrorMessage returns empty for invalid JSON")
    void parseGithubErrorMessage_invalidJson() {
        String invalidJson = "This is not JSON";
        Optional<String> result = api.parseGithubErrorMessage(invalidJson);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("parseGithubErrorMessage returns empty when message field is missing")
    void parseGithubErrorMessage_noMessageField() {
        String json = "{\"error\":\"Something went wrong\"}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("parseGithubErrorMessage returns empty when message is not a string")
    void parseGithubErrorMessage_messageNotString() {
        String json = "{\"message\":123}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("parseGithubErrorMessage handles empty JSON object")
    void parseGithubErrorMessage_emptyObject() {
        String json = "{}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("parseGithubErrorMessage handles null message value")
    void parseGithubErrorMessage_nullMessage() {
        String json = "{\"message\":null}";
        Optional<String> result = api.parseGithubErrorMessage(json);

        assertFalse(result.isPresent());
    }

    //# Tests for buildErrorMessage ------------------------------------

    @Test
    @DisplayName("buildErrorMessage formats 404 errors correctly")
    void buildErrorMessage_404() {
        String response = "{\"message\":\"Not Found\"}";
        String result = api.buildErrorMessage(404, "fetch teams",
                "Organization 'test-org'", response);

        assertEquals("Organization 'test-org' does not exist. GitHub says: Not Found", result);
    }

    @Test
    @DisplayName("buildErrorMessage formats 404 without GitHub message")
    void buildErrorMessage_404_noMessage() {
        String response = "{}";
        String result = api.buildErrorMessage(404, "fetch teams",
                "Organization 'test-org'", response);

        assertEquals("Organization 'test-org' does not exist.", result);
    }

    @Test
    @DisplayName("buildErrorMessage formats 401 errors correctly")
    void buildErrorMessage_401() {
        String response = "{\"message\":\"Bad credentials\"}";
        String result = api.buildErrorMessage(401, "fetch teams",
                "Organization 'test-org'", response);

        assertEquals("Authentication failed. Please check your access token. GitHub says: Bad credentials", result);
    }

    @Test
    @DisplayName("buildErrorMessage formats 403 errors correctly")
    void buildErrorMessage_403() {
        String response = "{\"message\":\"Forbidden\"}";
        String result = api.buildErrorMessage(403, "delete repository",
                "Repository 'test-repo'", response);

        assertEquals("Access forbidden. You don't have permission to delete repository. GitHub says: Forbidden",
                result);
    }

    @Test
    @DisplayName("buildErrorMessage formats 422 errors correctly")
    void buildErrorMessage_422() {
        String response = "{\"message\":\"Validation Failed\"}";
        String result = api.buildErrorMessage(422, "create team",
                "Team 'test-team'", response);

        assertEquals("Validation failed for create team. GitHub says: Validation Failed", result);
    }

    @Test
    @DisplayName("buildErrorMessage formats other status codes correctly")
    void buildErrorMessage_otherStatusCodes() {
        String response = "{\"message\":\"Internal Server Error\"}";
        String result = api.buildErrorMessage(500, "fetch repositories",
                "Organization 'test-org'", response);

        assertEquals("Failed to fetch repositories. Status: 500. GitHub says: Internal Server Error", result);
    }

    @Test
    @DisplayName("buildErrorMessage handles invalid JSON response body")
    void buildErrorMessage_invalidResponseBody() {
        String response = "Invalid JSON";
        String result = api.buildErrorMessage(404, "fetch teams",
                "Organization 'test-org'", response);

        assertEquals("Organization 'test-org' does not exist.", result);
    }

    @Test
    @DisplayName("buildErrorMessage handles multiple resource types for 404")
    void buildErrorMessage_404_multipleResources() {
        String response = "{\"message\":\"Not Found\"}";
        String result = api.buildErrorMessage(404, "fetch repositories for team",
                "Team 'team-1' or organization 'test-org'", response);

        assertEquals("Team 'team-1' or organization 'test-org' does not exist. GitHub says: Not Found", result);
    }

    @Test
    @DisplayName("buildErrorMessage formats 403 without GitHub message")
    void buildErrorMessage_403_noMessage() {
        String response = "{}";
        String result = api.buildErrorMessage(403, "access repository",
                "Repository 'test-repo'", response);

        assertEquals("Access forbidden. You don't have permission to access repository.", result);
    }

    @Test
    @DisplayName("buildErrorMessage handles status code with empty response")
    void buildErrorMessage_emptyResponse() {
        String response = "";
        String result = api.buildErrorMessage(503, "fetch data",
                "Service", response);

        assertEquals("Failed to fetch data. Status: 503.", result);
    }
}
