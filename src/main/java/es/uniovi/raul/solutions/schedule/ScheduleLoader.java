package es.uniovi.raul.solutions.schedule;

import static java.lang.String.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import org.apache.commons.csv.*;

import es.uniovi.raul.solutions.course.Schedule;

public class ScheduleLoader {

    public static Map<String, Schedule> load(String scheduleFile) throws IOException, InvalidScheduleFormatException {
        Map<String, Schedule> schedules = new HashMap<>();

        try (var reader = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(scheduleFile));
                var csvParser = new CSVParser(reader, CSVFormat.Builder.create()
                        .setIgnoreSurroundingSpaces(true)
                        .setTrim(true)
                        .setSkipHeaderRecord(false)
                        .build())) {

            for (CSVRecord csvRecord : csvParser) {

                var group = getValue(csvRecord, 0);
                var weekday = getValue(csvRecord, 1);
                var startTime = LocalTime.parse(getValue(csvRecord, 2));
                var minutes = Integer.parseInt(getValue(csvRecord, 3));

                schedules.put(group, new Schedule(weekday, startTime, minutes));
            }

            return schedules;

        } catch (RuntimeException e) {
            throw new InvalidScheduleFormatException(e.getMessage());
        }
    }

    private static String getValue(CSVRecord csvRecord, int column) throws InvalidScheduleFormatException {

        return findValue(csvRecord, column)
                .orElseThrow(() -> new InvalidScheduleFormatException(
                        format("Record #%d: '%s' -> column '%d' (zero based) cannot be blank",
                                csvRecord.getRecordNumber(), join(", ", csvRecord), column)));
    }

    private static Optional<String> findValue(CSVRecord csvRecord, int column) {

        if (!csvRecord.isSet(column))
            return Optional.empty();

        String value = csvRecord.get(column);
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);

    }

    public static class InvalidScheduleFormatException extends Exception {
        public InvalidScheduleFormatException(String message) {
            super(message);
        }
    }
}
