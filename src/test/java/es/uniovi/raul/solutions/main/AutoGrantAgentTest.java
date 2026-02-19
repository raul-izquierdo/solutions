package es.uniovi.raul.solutions.main;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;

import es.uniovi.raul.solutions.course.*;
import es.uniovi.raul.solutions.main.agents.*;

class AutoGrantAgentTest {

    private static Clock fixedClock(int year, int month, int day, int hour, int minute) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = LocalDate.of(year, month, day);
        LocalTime time = LocalTime.of(hour, minute);
        return Clock.fixed(LocalDateTime.of(date, time).atZone(zone).toInstant(), zone);
    }

    private static Course courseWithGroups(List<Group> groups, List<String> solutions) {
        Course course = mock(Course.class);
        when(course.groups()).thenReturn(groups);
        when(course.solutions()).thenReturn(solutions);
        when(course.solutionExists(anyString())).thenAnswer(inv -> solutions.contains(inv.getArgument(0)));

        return course;
    }

    private static Group group(String name, String day, int startHour, int startMinute, int minutes,
            String... hasAccess) {
        Group g = mock(Group.class);
        when(g.name()).thenReturn(name);
        Schedule s = new Schedule(day, LocalTime.of(startHour, startMinute), minutes);
        when(g.schedule()).thenReturn(Optional.of(s));
        when(g.isScheduledFor(anyString(), any()))
                .thenAnswer(inv -> s.includes(inv.getArgument(0), inv.getArgument(1)));
        Set<String> access = new HashSet<>(Arrays.asList(hasAccess));
        try {
            when(g.hasAccessTo(anyString())).thenAnswer(inv -> access.contains(inv.getArgument(0)));
            // Mock grantAccess to update the access set
            doAnswer(inv -> {
                String solution = inv.getArgument(0);
                access.add(solution);
                return null;
            }).when(g).grantAccess(anyString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return g;
    }

    private static Group groupNoSchedule(String name, String... hasAccess) {
        Group g = mock(Group.class);
        when(g.name()).thenReturn(name);
        when(g.schedule()).thenReturn(Optional.empty());
        when(g.isScheduledFor(anyString(), any())).thenReturn(false);
        Set<String> access = new HashSet<>(Arrays.asList(hasAccess));
        try {
            when(g.hasAccessTo(anyString())).thenAnswer(inv -> access.contains(inv.getArgument(0)));
            // Mock grantAccess to update the access set
            doAnswer(inv -> {
                String solution = inv.getArgument(0);
                access.add(solution);
                return null;
            }).when(g).grantAccess(anyString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return g;
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
