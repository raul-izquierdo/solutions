package es.uniovi.raul.solutions.schedule;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.uniovi.raul.solutions.course.Schedule;
import es.uniovi.raul.solutions.schedule.ScheduleLoader.InvalidScheduleFormat;

class ScheduleLoaderTest {

    @TempDir
    private Path tempDir;

    private Path writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("schedule.csv");
        Files.writeString(file, content);
        return file;
    }

    @Test
    @DisplayName("Loads various minutes formats and defaults blank minutes to 120")
    void loads_minutes_variants() throws Exception {
        Path csv = writeCsv(String.join(System.lineSeparator(),
                "G3, wednesday, 8:15, 2",
                "G1, monday, 10:00, 2h",
                "G2, tuesday, 09:30, 120m",
                "G3, wednesday, 08:15, 2",
                "G3, wednesday, 8, 2",
                "G4, thursday, 12:00," // blank -> default 120
        ));

        Map<String, Schedule> map = ScheduleLoader.load(csv.toString());
        assertEquals(4, map.size());

        assertEquals(LocalTime.of(10, 0), map.get("G1").startTime());
        assertEquals(120, map.get("G1").minutes());

        assertEquals(120, map.get("G2").minutes());
        assertEquals(120, map.get("G3").minutes());
        assertEquals(120, map.get("G4").minutes());
    }

    @Test
    @DisplayName("Invalid formats raise InvalidScheduleFormat with record and column details")
    void invalid_formats() throws Exception {
        // Invalid minutes token and missing fields
        Path csv = writeCsv(String.join(System.lineSeparator(),
                "G1, friday, 10:00, ZZ",
                ", monday, 10:00, 60",
                "G3, monday, , 60"));

        InvalidScheduleFormat ex1 = assertThrows(InvalidScheduleFormat.class,
                () -> ScheduleLoader.load(csv.toString()));
        assertTrue(ex1.getMessage().contains("invalid format"));
    }
}
