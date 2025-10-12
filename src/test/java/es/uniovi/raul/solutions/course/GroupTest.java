package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.github.GithubApi;
import es.uniovi.raul.solutions.github.GithubApi.RejectedOperationException;
import es.uniovi.raul.solutions.github.GithubApi.UnexpectedFormatException;

class GroupTest {

    private Course mockCourse(GithubApi api, String orgName, List<String> allRepos)
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        when(api.fetchTeams(orgName)).thenReturn(List.of()); // not used for this helper
        when(api.fetchAllRepositories(orgName)).thenReturn(allRepos);
        return new Course(orgName, api);
    }

    @Test
    @DisplayName("Group exposes name, schedule, and accessible solutions from API")
    void groupPropertiesAndAccessibleSolutions() throws Exception {
        GithubApi api = mock(GithubApi.class);
        String org = "org";

        // Course with two solution repos available
        Course course = mockCourse(api, org, List.of("a-solution", "b-solution", "lib"));

        // Team repositories returned for this group (with org prefix and raw)
        when(api.fetchRepositoriesForTeam(org, "slug1")).thenReturn(List.of(
                "org/a-solution",
                "b-solution",
                "misc"));

        Group g = new Group("G1", Optional.of(new Schedule("monday", LocalTime.of(9, 0), 30)), "slug1", course);

        assertEquals("G1", g.name());
        assertTrue(g.schedule().isPresent());

        // Only solution repos, and repo name is trimmed from org/
        assertEquals(List.of("a-solution", "b-solution"), g.getAccesibleSolutions());
        assertTrue(g.hasAccessTo("a-solution"));
        assertFalse(g.hasAccessTo("lib"));

        // Scheduling helper
        assertTrue(g.isScheduledFor("monday", LocalTime.of(9, 15)));
        assertFalse(g.isScheduledFor("tuesday", LocalTime.of(9, 15)));
    }

    @Test
    @DisplayName("Group.grantAccess delegates to GithubConnection when solution exists; rejects invalid solution")
    void grantAccessSuccessAndFailure() throws Exception {
        GithubApi api = mock(GithubApi.class);
        String org = "org";

        Course course = mockCourse(api, org, List.of("katas-solution"));
        when(api.fetchRepositoriesForTeam(org, "slug")).thenReturn(List.of());

        Group g = new Group("G", Optional.empty(), "slug", course);

        // Success path delegates
        g.grantAccess("katas-solution");
        verify(api).grantAccess(org, "katas-solution", "slug");

        // Failure when solution doesn't exist in course
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> g.grantAccess("nope"));
        assertTrue(ex.getMessage().contains("not a valid solution"));
    }

    @Test
    @DisplayName("Group.revokeAccess delegates with correct parameter order and rejects invalid solution")
    void revokeAccessSuccessAndFailure() throws Exception {
        GithubApi api = mock(GithubApi.class);
        String org = "org";

        Course course = mockCourse(api, org, List.of("katas-solution"));
        when(api.fetchRepositoriesForTeam(org, "slug")).thenReturn(List.of());

        Group g = new Group("G", Optional.empty(), "slug", course);

        g.revokeAccess("katas-solution");
        // Signature is (organization, repository, teamSlug)
        verify(api).revokeAccess(org, "katas-solution", "slug");

        assertThrows(IllegalArgumentException.class, () -> g.revokeAccess("nope"));
    }

    @Test
    @DisplayName("Constructor null checks")
    void constructorNullChecks() {
        GithubApi api = mock(GithubApi.class);
        try {
            when(api.fetchTeams("org")).thenReturn(List.of());
            when(api.fetchAllRepositories("org")).thenReturn(List.of());
            when(api.fetchRepositoriesForTeam(anyString(), anyString())).thenReturn(List.of());
            Course course = new Course("org", api);

            assertThrows(IllegalArgumentException.class, () -> new Group(null, Optional.empty(), "slug", course));
            assertThrows(IllegalArgumentException.class, () -> new Group("G", null, "slug", course));
            assertThrows(IllegalArgumentException.class, () -> new Group("G", Optional.empty(), null, course));
            assertThrows(IllegalArgumentException.class, () -> new Group("G", Optional.empty(), "slug", null));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    @DisplayName("Group with no accessible solutions returns empty list and hasAccessTo=false")
    void noAccessibleSolutions() throws Exception {
        GithubApi api = mock(GithubApi.class);
        Course course = mockCourse(api, "org", List.of("a-solution"));
        when(api.fetchRepositoriesForTeam("org", "slug")).thenReturn(List.of("org/other"));

        Group g = new Group("G", Optional.empty(), "slug", course);
        assertTrue(g.getAccesibleSolutions().isEmpty());
        assertFalse(g.hasAccessTo("a-solution"));
    }

    @Test
    @DisplayName("isScheduledFor is case-insensitive for day and checks inclusive bounds")
    void isScheduledCaseAndBounds() throws Exception {
        GithubApi api = mock(GithubApi.class);
        Course course = mockCourse(api, "org", List.of());
        Group g = new Group("G", Optional.of(new Schedule("MONDAY", LocalTime.of(12, 0), 30)), "slug", course);
        assertTrue(g.isScheduledFor("monday", LocalTime.of(12, 0)));
        assertTrue(g.isScheduledFor("MONDAY", LocalTime.of(12, 30)));
        assertFalse(g.isScheduledFor("monday", LocalTime.of(12, 31)));
    }

    @Test
    @DisplayName("Grant/revoke propagate API exceptions")
    void delegateErrorsPropagate() throws Exception {
        GithubApi api = mock(GithubApi.class);
        when(api.fetchTeams("org")).thenReturn(List.of());
        when(api.fetchAllRepositories("org")).thenReturn(List.of("a-solution"));
        when(api.fetchRepositoriesForTeam(anyString(), anyString())).thenReturn(List.of());
        Course course = new Course("org", api);
        Group g = new Group("G", Optional.empty(), "slug", course);

        doThrow(new GithubApi.RejectedOperationException("boom")).when(api)
                .grantAccess("org", "a-solution", "slug");
        assertThrows(GithubApi.RejectedOperationException.class, () -> g.grantAccess("a-solution"));

        doThrow(new GithubApi.UnexpectedFormatException("oops")).when(api)
                .revokeAccess("org", "a-solution", "slug");
        assertThrows(GithubApi.UnexpectedFormatException.class, () -> g.revokeAccess("a-solution"));
    }
}
