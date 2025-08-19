package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uniovi.raul.solutions.github.GithubConnection;
import es.uniovi.raul.solutions.github.GithubConnection.RejectedOperationException;
import es.uniovi.raul.solutions.github.GithubConnection.UnexpectedFormatException;
import es.uniovi.raul.solutions.github.Team;

class CourseTest {

    @Test
    @DisplayName("Course loads groups filtered by 'group ' prefix and loads solutions by naming rule")
    void courseLoadsGroupsAndSolutions()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        GithubConnection api = mock(GithubConnection.class);

        // Teams: only those that start with "group " become Course groups
        when(api.fetchTeams("org")).thenReturn(List.of(
                new Team("group A1", "a1"),
                new Team("group B2", "b2"),
                new Team("random team", "r1")));

        // Repositories: filter by SolutionsNaming.isSolutionRepository (endsWith("solution"))
        when(api.fetchAllRepositories("org")).thenReturn(List.of(
                "katas-solution",
                "project",
                "lab1-solution"));

        Map<String, Schedule> schedule = Map.of(
                "A1", new Schedule("monday", LocalTime.of(10, 0), 60));

        Course course = new Course("org", api, schedule);

        // Groups
        var groups = course.getGroups();
        assertEquals(2, groups.size());
        assertTrue(groups.stream().anyMatch(g -> g.name().equals("A1")));
        assertTrue(groups.stream().anyMatch(g -> g.name().equals("B2")));

        // Schedule mapping applies only to A1
        Optional<Schedule> schA1 = groups.stream().filter(g -> g.name().equals("A1")).findFirst()
                .flatMap(Group::schedule);
        assertTrue(schA1.isPresent());
        assertEquals(LocalTime.of(11, 0), schA1.get().getEndTime());

        Optional<Schedule> schB2 = groups.stream().filter(g -> g.name().equals("B2")).findFirst()
                .flatMap(Group::schedule);
        assertTrue(schB2.isEmpty());

        // Solutions list filtered
        var sols = course.getAllSolutions();
        assertEquals(List.of("katas-solution", "lab1-solution"), sols);

        // solutionExists helper
        assertTrue(course.solutionExists("katas-solution"));
        assertFalse(course.solutionExists("project"));
    }

    @Test
    @DisplayName("Course propagates API exceptions from GithubConnection")
    void coursePropagatesApiErrors()
            throws UnexpectedFormatException, RejectedOperationException, IOException, InterruptedException {
        GithubConnection api = mock(GithubConnection.class);
        when(api.fetchTeams("org")).thenThrow(new RejectedOperationException("rate limited"));

        assertThrows(RejectedOperationException.class, () -> new Course("org", api));
    }

    @Test
    @DisplayName("Null checks in Course constructor")
    void courseNullChecks() {
        GithubConnection api = mock(GithubConnection.class);
        assertThrows(IllegalArgumentException.class, () -> new Course(null, api));
        assertThrows(IllegalArgumentException.class, () -> new Course("org", null));
        assertThrows(IllegalArgumentException.class, () -> new Course("org", api, null));
    }

    @Test
    @DisplayName("Course handles empty teams and repositories, and ignores non-group teams")
    void courseHandlesEmptyAndNonGroup() throws Exception {
        GithubConnection api = mock(GithubConnection.class);
        when(api.fetchTeams("org")).thenReturn(List.of(
                new Team("random", "r"),
                new Team("group ", "empty"), // becomes empty group name
                new Team("group X", "x"),
                new Team("group X", "x-dup") // duplicate display name allowed; results in two groups with same name
        ));
        when(api.fetchAllRepositories("org")).thenReturn(List.of());

        Course course = new Course("org", api, Map.of());

        var groups = course.getGroups();
        // Only those starting with "group " and something else after it are valid
        assertEquals(2, groups.size());
        assertEquals(2, groups.stream().filter(g -> g.name().equals("X")).count());

        assertTrue(course.getAllSolutions().isEmpty());
    }
}
