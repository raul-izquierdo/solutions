package es.uniovi.raul.solutions.schedule;

import static java.lang.Integer.*;
import static java.lang.String.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.csv.*;

import es.uniovi.raul.solutions.course.Schedule;

public class ScheduleLoader {

    public static Map<String, Schedule> load(String scheduleFile) throws IOException, InvalidScheduleFormat {
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
                var minutes = toMinutes(csvRecord, 3);

                schedules.put(group, new Schedule(weekday, startTime, minutes));
            }

            return schedules;

        }
    }

    private static int toMinutes(CSVRecord csvRecord, int column) throws InvalidScheduleFormat {

        var value = findValue(csvRecord, column);
        if (value.isEmpty())
            return 60 * 2; // 2 hours

        // The value can be "2" (hours), "2h" (hours) or "2m" (minutes)
        var matcher = Pattern.compile("^(\\d+)(h|m)?$").matcher(value.get());
        if (!matcher.matches())
            throw newFormatException(csvRecord, column, "<integer> or <integer>h or <integer>m>");

        int minutes = parseInt(matcher.group(1));
        if ("m".equals(matcher.group(2)))
            return minutes;

        return minutes * 60;

    }

    private static String getValue(CSVRecord csvRecord, int column) throws InvalidScheduleFormat {

        return findValue(csvRecord, column)
                .orElseThrow(() -> newFormatException(csvRecord, column, "cannot be blank"));
    }

    private static Optional<String> findValue(CSVRecord csvRecord, int column) {

        if (!csvRecord.isSet(column))
            return Optional.empty();

        String value = csvRecord.get(column);
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);

    }

    private static InvalidScheduleFormat newFormatException(CSVRecord csvRecord, int column, String message) {

        return new InvalidScheduleFormat(
                format("Record #%d: '%s' -> column '%d' (zero based) has an invalid format: %s",
                        csvRecord.getRecordNumber(),
                        join(", ", csvRecord),
                        column,
                        message));
    }

    public static class InvalidScheduleFormat extends Exception {
        public InvalidScheduleFormat(String message) {
            super(message);
        }
    }
}
