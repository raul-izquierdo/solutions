package es.uniovi.raul.solutions.main;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;

import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.course.naming.SolutionsDetectionStrategy;
import es.uniovi.raul.solutions.github.GithubApi;
import es.uniovi.raul.solutions.main.agents.*;

class AutoGrantAgentTest {

    private static Clock fixedClock(int year, int month, int day, int hour, int minute) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = LocalDate.of(year, month, day);
        LocalTime time = LocalTime.of(hour, minute);
        return Clock.fixed(LocalDateTime.of(date, time).atZone(zone).toInstant(), zone);
    }

    private static Course courseWithGroups(List<Group> groups, List<String> solutions) {
        return new Course(groups, solutions);
    }

    private static Group group(String name, String day, int startHour, int startMinute, int minutes,
            String... hasAccess) throws Exception {
        GithubApi mockApi = mock(GithubApi.class);
        Schedule schedule = new Schedule(day, LocalTime.of(startHour, startMinute), minutes);

        // Use a shared mutable list that will be modified by grantAccess/revokeAccess
        List<String> accessList = new ArrayList<>(Arrays.asList(hasAccess));

        // Return the current state of the list each time it's fetched
        when(mockApi.fetchRepositoriesForTeam(anyString(), anyString()))
                .thenAnswer(inv -> new ArrayList<>(accessList));

        // Update the list when grantAccess is called
        doAnswer(inv -> {
            String solution = inv.getArgument(1); // repository name is second argument
            if (!accessList.contains(solution)) {
                accessList.add(solution);
            }
            return null;
        }).when(mockApi).grantAccess(anyString(), anyString(), anyString());

        // Update the list when revokeAccess is called
        doAnswer(inv -> {
            String solution = inv.getArgument(1); // repository name is second argument
            accessList.remove(solution);
            return null;
        }).when(mockApi).revokeAccess(anyString(), anyString(), anyString());

        SolutionsDetectionStrategy mockDetector = mock(SolutionsDetectionStrategy.class);
        when(mockDetector.isSolutionRepository(anyString())).thenReturn(true);

        return new Group(name, "team-" + name.toLowerCase(), Optional.of(schedule),
                mockApi, "test-org", mockDetector);
    }

    private static Group groupNoSchedule(String name, String... hasAccess) throws Exception {
        GithubApi mockApi = mock(GithubApi.class);

        // Use a shared mutable list that will be modified by grantAccess/revokeAccess
        List<String> accessList = new ArrayList<>(Arrays.asList(hasAccess));

        // Return the current state of the list each time it's fetched
        when(mockApi.fetchRepositoriesForTeam(anyString(), anyString()))
                .thenAnswer(inv -> new ArrayList<>(accessList));

        // Update the list when grantAccess is called
        doAnswer(inv -> {
            String solution = inv.getArgument(1); // repository name is second argument
            if (!accessList.contains(solution)) {
                accessList.add(solution);
            }
            return null;
        }).when(mockApi).grantAccess(anyString(), anyString(), anyString());

        // Update the list when revokeAccess is called
        doAnswer(inv -> {
            String solution = inv.getArgument(1); // repository name is second argument
            accessList.remove(solution);
            return null;
        }).when(mockApi).revokeAccess(anyString(), anyString(), anyString());

        SolutionsDetectionStrategy mockDetector = mock(SolutionsDetectionStrategy.class);
        when(mockDetector.isSolutionRepository(anyString())).thenReturn(true);

        return new Group(name, "team-" + name.toLowerCase(), Optional.empty(),
                mockApi, "test-org", mockDetector);
    }

    @Test
    @DisplayName("tryAutomaticSelection happy path: one group scheduled, picks first not-yet-accessed solution, confirms true")
    void tryAutomaticSelection_happy_path() throws Exception {
        Clock clock = fixedClock(2025, 8, 18, 10, 0); // Monday 10:00
        Prompter prompter = mock(Prompter.class);
        when(prompter.confirm(anyString(), any(Object[].class))).thenReturn(true);

        Group g1 = group("G1", "monday", 10, 0, 60, "a-solution");
        Group g2 = groupNoSchedule("G2");
        Course course = courseWithGroups(List.of(g1, g2), List.of("a-solution", "b-solution"));

        AutoGrantAgent agent = new AutoGrantAgent(clock, prompter);
        boolean result = agent.tryAutomaticSelection(course);

        assertTrue(result);
        assertTrue(g1.hasAccessTo("b-solution"));
        verify(prompter).confirm(contains(" You are currently with group"), any(Object[].class));
    }

    @Test
    @DisplayName("tryAutomaticSelection returns false when no single scheduled group or no solution to grant or user cancels")
    void tryAutomaticSelection_edge_cases() throws Exception {
        Clock clock = fixedClock(2025, 8, 18, 10, 0); // Monday 10:00
        Prompter prompter = mock(Prompter.class);
        AutoGrantAgent agent = new AutoGrantAgent(clock, prompter);

        // 1) No scheduled group
        Course c1 = courseWithGroups(List.of(groupNoSchedule("G1"), groupNoSchedule("G2")), List.of("x-solution"));
        assertFalse(agent.tryAutomaticSelection(c1));

        // 2) Multiple scheduled groups -> ambiguous
        Course c2 = courseWithGroups(List.of(
                group("A", "monday", 10, 0, 60),
                group("B", "monday", 9, 30, 60)), List.of("x-solution"));
        assertFalse(agent.tryAutomaticSelection(c2));

        // 3) No remaining solution to grant
        Course c3 = courseWithGroups(List.of(group("A", "monday", 10, 0, 60, "a-solution")), List.of("a-solution"));
        assertFalse(agent.tryAutomaticSelection(c3));

        // 4) User cancels
        when(prompter.confirm(anyString(), any(Object[].class))).thenReturn(false);
        Course c4 = courseWithGroups(List.of(group("A", "monday", 10, 0, 60)), List.of("a-solution"));
        assertFalse(agent.tryAutomaticSelection(c4));
    }

    @Test
    @DisplayName("guessGroup chooses unique scheduled group; guessSolution chooses lexicographically smallest not-yet-accessed")
    void guessers() throws Exception {
        Clock clock = fixedClock(2025, 8, 18, 10, 0); // Monday
        AutoGrantAgent agent = new AutoGrantAgent(clock, (m, a) -> true);

        Group a = group("A", "monday", 10, 0, 60, "b-solution");
        Group b = group("B", "monday", 9, 0, 60);

        assertEquals(Optional.empty(), agent.guessGroup(List.of()));
        assertTrue(agent.guessGroup(List.of(b, a)).isEmpty()); // two scheduled
        assertEquals("A", agent.guessGroup(List.of(a)).get().name());

        var next = agent.guessSolution(a, List.of("a-solution", "b-solution"));
        assertEquals(Optional.of("a-solution"), next);
    }
}
