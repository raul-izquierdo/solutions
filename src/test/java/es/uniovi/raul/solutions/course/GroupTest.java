package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.course.naming.SolutionsDetectionStrategy;
import es.uniovi.raul.solutions.github.GithubApi;

class GroupTest {

    private Group createTestGroup(String name, String teamSlug, List<String> expectedSolutions,
            Optional<Schedule> schedule) {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy identifier = mock(SolutionsDetectionStrategy.class);
        String org = "test-org";

        try {
            // Mock the GitHub API to return expected solutions
            when(api.fetchRepositoriesForTeam(org, teamSlug)).thenReturn(
                    expectedSolutions.stream()
                            .map(sol -> org + "/" + sol) // Add org prefix
                            .toList());
            when(identifier.isSolutionRepository(anyString())).thenReturn(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Group(name, teamSlug, schedule, api, org, identifier);
    }

    @Test
    @DisplayName("Group exposes name, schedule, and accessible solutions from API")
    void groupPropertiesAndAccessibleSolutions() throws Exception {
        Group g = createTestGroup("G1", "slug1", List.of("a-solution", "b-solution"),
                Optional.of(new Schedule("monday", LocalTime.of(9, 0), 30)));

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
    @DisplayName("Constructor null checks")
    void constructorNullChecks() {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy identifier = mock(SolutionsDetectionStrategy.class);

        assertThrows(IllegalArgumentException.class,
                () -> new Group(null, "slug", Optional.empty(), api, "org", identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", null, Optional.empty(), api, "org", identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", null, api, "org", identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), api, null, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), null, "org", identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), api, "org", null));
    }

    @Test
    @DisplayName("Group with no accessible solutions returns empty list and hasAccessTo=false")
    void noAccessibleSolutions() throws Exception {
        Group g = createTestGroup("G", "slug", List.of(), Optional.empty());
        assertTrue(g.getAccesibleSolutions().isEmpty());
        assertFalse(g.hasAccessTo("a-solution"));
    }

    @Test
    @DisplayName("isScheduledFor is case-insensitive for day and checks inclusive bounds")
    void isScheduledCaseAndBounds() {
        Group g = createTestGroup("G", "slug", List.of(),
                Optional.of(new Schedule("MONDAY", LocalTime.of(12, 0), 30)));
        assertTrue(g.isScheduledFor("monday", LocalTime.of(12, 0)));
        assertTrue(g.isScheduledFor("MONDAY", LocalTime.of(12, 30)));
        assertFalse(g.isScheduledFor("monday", LocalTime.of(12, 31)));
    }

    @Test
    @DisplayName("Group.grantAccess delegates to GithubApi and invalidates cache")
    void grantAccessDelegatesAndInvalidatesCache() throws Exception {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy identifier = mock(SolutionsDetectionStrategy.class);

        when(api.fetchRepositoriesForTeam("org", "team-slug"))
                .thenReturn(List.of("org/solution1"))
                .thenReturn(List.of("org/solution1", "org/solution2"));
        when(identifier.isSolutionRepository(anyString())).thenReturn(true);

        Group group = new Group("G1", "team-slug", Optional.empty(), api, "org", identifier);

        // Initial state - only solution1
        assertEquals(List.of("solution1"), group.getAccesibleSolutions());

        // Grant access to solution2
        group.grantAccess("solution2");
        verify(api).grantAccess("org", "solution2", "team-slug");

        // Cache should be invalidated and refetch on next call
        assertEquals(List.of("solution1", "solution2"), group.getAccesibleSolutions());
        verify(api, times(2)).fetchRepositoriesForTeam("org", "team-slug");
    }

    @Test
    @DisplayName("Group.revokeAccess delegates to GithubApi and invalidates cache")
    void revokeAccessDelegatesAndInvalidatesCache() throws Exception {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy identifier = mock(SolutionsDetectionStrategy.class);

        when(api.fetchRepositoriesForTeam("org", "team-slug"))
                .thenReturn(List.of("org/solution1", "org/solution2"))
                .thenReturn(List.of("org/solution1"));
        when(identifier.isSolutionRepository(anyString())).thenReturn(true);

        Group group = new Group("G1", "team-slug", Optional.empty(), api, "org", identifier);

        // Initial state - both solutions
        assertEquals(List.of("solution1", "solution2"), group.getAccesibleSolutions());

        // Revoke access to solution2
        group.revokeAccess("solution2");
        verify(api).revokeAccess("org", "solution2", "team-slug");

        // Cache should be invalidated and refetch on next call
        assertEquals(List.of("solution1"), group.getAccesibleSolutions());
        verify(api, times(2)).fetchRepositoriesForTeam("org", "team-slug");
    }

    @Test
    @DisplayName("Group.grantAccess null check")
    void grantAccessNullCheck() {
        Group g = createTestGroup("G", "slug", List.of(), Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> g.grantAccess(null));
    }

    @Test
    @DisplayName("Group.revokeAccess null check")
    void revokeAccessNullCheck() {
        Group g = createTestGroup("G", "slug", List.of(), Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> g.revokeAccess(null));
    }

}
