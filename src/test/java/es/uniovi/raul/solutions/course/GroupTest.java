package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.github.GithubApi;

class GroupTest {

    @Test
    @DisplayName("Group exposes name, schedule, and accessible solutions from API")
    void groupPropertiesAndAccessibleSolutions() throws Exception {
        GithubApi api = mock(GithubApi.class);
        String org = "org";

        // Team repositories returned for this group (with org prefix and raw)
        when(api.fetchRepositoriesForTeam(org, "slug1")).thenReturn(List.of(
                "org/a-solution",
                "b-solution",
                "misc"));

        Group g = new Group("G1", "slug1", List.of("a-solution", "b-solution"),
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
        assertThrows(IllegalArgumentException.class,
                () -> new Group(null, "slug", List.of(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new Group("G", "slug", List.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new Group("G", null, List.of(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new Group("G", "slug", null, Optional.empty()));
    }

    @Test
    @DisplayName("Group with no accessible solutions returns empty list and hasAccessTo=false")
    void noAccessibleSolutions() {
        Group g = new Group("G", "slug", List.of(), Optional.empty());
        assertTrue(g.getAccesibleSolutions().isEmpty());
        assertFalse(g.hasAccessTo("a-solution"));
    }

    @Test
    @DisplayName("isScheduledFor is case-insensitive for day and checks inclusive bounds")
    void isScheduledCaseAndBounds() {
        Group g = new Group("G", "slug", List.of(),
                Optional.of(new Schedule("MONDAY", LocalTime.of(12, 0), 30)));
        assertTrue(g.isScheduledFor("monday", LocalTime.of(12, 0)));
        assertTrue(g.isScheduledFor("MONDAY", LocalTime.of(12, 30)));
        assertFalse(g.isScheduledFor("monday", LocalTime.of(12, 31)));
    }

}
