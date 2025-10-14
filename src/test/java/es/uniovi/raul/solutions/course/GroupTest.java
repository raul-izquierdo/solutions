package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.course.naming.SolutionIdentifier;
import es.uniovi.raul.solutions.github.GithubApi;

class GroupTest {

    private Group createTestGroup(String name, String teamSlug, List<String> expectedSolutions,
            Optional<Schedule> schedule) {
        GithubApi api = mock(GithubApi.class);
        SolutionIdentifier identifier = mock(SolutionIdentifier.class);
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

        return new Group(name, teamSlug, schedule, org, api, identifier);
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
        SolutionIdentifier identifier = mock(SolutionIdentifier.class);

        assertThrows(IllegalArgumentException.class,
                () -> new Group(null, "slug", Optional.empty(), "org", api, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", null, Optional.empty(), "org", api, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", null, "org", api, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), null, api, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), "org", null, identifier));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", "slug", Optional.empty(), "org", api, null));
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

}
