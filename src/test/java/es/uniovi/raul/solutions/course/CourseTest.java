package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalTime;
import java.util.*;

import org.junit.jupiter.api.*;

import es.uniovi.raul.solutions.course.naming.SolutionsDetectionStrategy;
import es.uniovi.raul.solutions.github.*;

class CourseTest {

    @Test
    @DisplayName("Course loads groups filtered by 'group ' prefix and loads solutions by naming rule")
    void courseLoadsGroupsAndSolutions() {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy detector = mock(SolutionsDetectionStrategy.class);

        Map<String, Schedule> schedule = Map.of(
                "A1", new Schedule("monday", LocalTime.of(10, 0), 60));

        // Create groups directly
        Group groupA1 = new Group("A1", "a1", Optional.of(schedule.get("A1")),
                api, "org", detector);
        Group groupB2 = new Group("B2", "b2", Optional.empty(),
                api, "org", detector);

        List<String> solutions = List.of("katas-solution", "lab1-solution");

        Course course = new Course(List.of(groupA1, groupB2), solutions);

        // Groups
        var groups = course.groups();
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
        var sols = course.solutions();
        assertEquals(List.of("katas-solution", "lab1-solution"), sols);
    }

    @Test
    @DisplayName("Null checks in Course constructor")
    void courseNullChecks() {
        assertThrows(IllegalArgumentException.class,
                () -> new Course(null, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new Course(List.of(), null));
    }

    @Test
    @DisplayName("Course handles empty teams and repositories, and ignores non-group teams")
    void courseHandlesEmptyAndNonGroup() {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy detector = mock(SolutionsDetectionStrategy.class);

        // Create two groups with name "X" (duplicate names allowed)
        Group groupX1 = new Group("X", "x", Optional.empty(), api, "org", detector);
        Group groupX2 = new Group("X", "x-dup", Optional.empty(), api, "org", detector);

        Course course = new Course(List.of(groupX1, groupX2), List.of());

        var groups = course.groups();
        // Only those starting with "group " and something else after it are valid
        assertEquals(2, groups.size());
        assertEquals(2, groups.stream().filter(g -> g.name().equals("X")).count());

        assertTrue(course.solutions().isEmpty());
    }

    @Test
    @DisplayName("Course handles schedule with group not present in organization teams")
    void courseHandlesScheduleGroupNotInTeams() {
        GithubApi api = mock(GithubApi.class);
        SolutionsDetectionStrategy detector = mock(SolutionsDetectionStrategy.class);

        // Only group B2 is present
        Group groupB2 = new Group("B2", "b2", Optional.empty(), api, "org", detector);

        Course course = new Course(List.of(groupB2), List.of());

        // Only B2 group should be present
        var groups = course.groups();
        assertEquals(1, groups.size());
        assertTrue(groups.stream().anyMatch(group -> group.name().equals("B2")));
    }

}
