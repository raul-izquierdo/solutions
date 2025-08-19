package es.uniovi.raul.solutions.course;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleTest {

    @Test
    @DisplayName("Schedule constructor validates inputs and normalizes day of week")
    void constructorSuccessAndNormalization() {
        Schedule s = new Schedule("Monday", LocalTime.of(10, 0), 60);
        assertEquals("monday", s.dayOfWeek());
        assertEquals(LocalTime.of(10, 0), s.startTime());
        assertEquals(60, s.minutes());
        assertEquals(LocalTime.of(11, 0), s.getEndTime());
    }

    @Test
    @DisplayName("Schedule.includes matches day and time within range (inclusive)")
    void includesHappyPath() {
        Schedule s = new Schedule("wednesday", LocalTime.of(9, 30), 30);
        assertTrue(s.includes("Wednesday", LocalTime.of(9, 30))); // start inclusive
        assertTrue(s.includes("wednesday", LocalTime.of(10, 0))); // end inclusive
        assertFalse(s.includes("wednesday", LocalTime.of(10, 1)));
        assertFalse(s.includes("thursday", LocalTime.of(9, 45)));
    }

    @Test
    @DisplayName("Boundary start times are allowed: 08:00 and 21:00")
    void boundary_start_times_allowed() {
        Schedule s1 = new Schedule("monday", LocalTime.of(8, 0), 11);
        assertEquals(LocalTime.of(8, 11), s1.getEndTime());

        Schedule s2 = new Schedule("tuesday", LocalTime.of(21, 0), 11);
        assertEquals(LocalTime.of(21, 11), s2.getEndTime());
    }

    @Test
    @DisplayName("Minutes boundaries: 11 min is min allowed, 360 max allowed")
    void minutes_boundaries_allowed() {
        Schedule smin = new Schedule("friday", LocalTime.of(8, 0), 11);
        assertEquals(11, smin.minutes());

        Schedule smax = new Schedule("friday", LocalTime.of(8, 0), 360);
        assertEquals(360, smax.minutes());
    }

    @Test
    @DisplayName("Invalid day of week throws")
    void invalidDayFails() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Schedule("funday", LocalTime.of(10, 0), 60));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid day"));
    }

    @Test
    @DisplayName("Start time outside [8,21] fails")
    void invalidStartTimeFails() {
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", LocalTime.of(7, 59), 30));
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", LocalTime.of(22, 0), 30));
    }

    @Test
    @DisplayName("Minutes must be in (10, 360]")
    void invalidMinutesFails() {
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", LocalTime.of(8, 0), 10));
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", LocalTime.of(8, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", LocalTime.of(8, 0), 361));
    }

    @Test
    @DisplayName("Null checks via Debug.notNull")
    void nullChecks() {
        assertThrows(IllegalArgumentException.class, () -> new Schedule(null, LocalTime.of(8, 0), 30));
        assertThrows(IllegalArgumentException.class, () -> new Schedule("monday", null, 30));
        Schedule s = new Schedule("monday", LocalTime.of(8, 0), 30);
        assertThrows(IllegalArgumentException.class, () -> s.includes(null, LocalTime.of(8, 0)));
        assertThrows(IllegalArgumentException.class, () -> s.includes("monday", null));
    }

    @Test
    @DisplayName("isValidDayOfWeek is case-insensitive and rejects invalid")
    void validDayOfWeekStatic() {
        assertTrue(Schedule.isValidDayOfWeek("Monday"));
        assertTrue(Schedule.isValidDayOfWeek("sUnDaY"));
        assertFalse(Schedule.isValidDayOfWeek("funday"));
        assertThrows(IllegalArgumentException.class, () -> Schedule.isValidDayOfWeek(null));
    }
}
