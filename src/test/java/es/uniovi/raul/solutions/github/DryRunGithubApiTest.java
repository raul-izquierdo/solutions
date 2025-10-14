package es.uniovi.raul.solutions.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link DryRunGithubApi} to verify that read operations are delegated
 * and write operations only print messages.
 */
class DryRunGithubApiTest {

    @Test
    @DisplayName("Read operations are delegated to the underlying API")
    void read_operations_delegated() throws Exception {
        GithubApi mockDelegate = mock(GithubApi.class);
        when(mockDelegate.fetchTeams("test-org")).thenReturn(List.of(new Team("team1", "team-1")));
        when(mockDelegate.fetchAllRepositories("test-org")).thenReturn(List.of("repo1", "repo2"));
        when(mockDelegate.fetchRepositoriesForTeam("test-org", "team-1")).thenReturn(List.of("repo1"));

        DryRunGithubApi dryRunApi = new DryRunGithubApi(mockDelegate);

        // Verify read operations are delegated
        assertEquals(List.of(new Team("team1", "team-1")), dryRunApi.fetchTeams("test-org"));
        assertEquals(List.of("repo1", "repo2"), dryRunApi.fetchAllRepositories("test-org"));
        assertEquals(List.of("repo1"), dryRunApi.fetchRepositoriesForTeam("test-org", "team-1"));

        // Verify the delegate was called
        verify(mockDelegate).fetchTeams("test-org");
        verify(mockDelegate).fetchAllRepositories("test-org");
        verify(mockDelegate).fetchRepositoriesForTeam("test-org", "team-1");
    }

    @Test
    @DisplayName("Write operations print messages instead of calling the delegate")
    void write_operations_print_messages() throws Exception {
        GithubApi mockDelegate = mock(GithubApi.class);
        DryRunGithubApi dryRunApi = new DryRunGithubApi(mockDelegate);

        // Capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            // Test grantAccess
            dryRunApi.grantAccess("test-org", "test-repo", "test-team");
            assertTrue(outContent.toString().contains("[DRY RUN]"));
            assertTrue(outContent.toString().contains("grant access"));
            assertTrue(outContent.toString().contains("test-team"));
            assertTrue(outContent.toString().contains("test-org/test-repo"));

            outContent.reset();

            // Test revokeAccess
            dryRunApi.revokeAccess("test-org", "test-repo", "test-team");
            assertTrue(outContent.toString().contains("[DRY RUN]"));
            assertTrue(outContent.toString().contains("revoke access"));
            assertTrue(outContent.toString().contains("test-team"));
            assertTrue(outContent.toString().contains("test-org/test-repo"));

            // Verify the delegate was NOT called
            verify(mockDelegate, never()).grantAccess(anyString(), anyString(), anyString());
            verify(mockDelegate, never()).revokeAccess(anyString(), anyString(), anyString());

        } finally {
            System.setOut(originalOut);
        }
    }
}
