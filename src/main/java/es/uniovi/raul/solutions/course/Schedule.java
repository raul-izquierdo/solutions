package es.uniovi.raul.solutions.course;

import static es.uniovi.raul.solutions.debug.Debug.*;

import java.time.LocalTime;
import java.util.stream.Stream;

public record Schedule(String dayOfWeek, LocalTime startTime, int minutes) {

    public Schedule {
        notNull(dayOfWeek, startTime, minutes);

        dayOfWeek = dayOfWeek.toLowerCase();

        if (!isValidDayOfWeek(dayOfWeek))
            throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeek);

        if (startTime.getHour() < 8 || startTime.getHour() > 21)
            throw new IllegalArgumentException("Start time must be between 8:00 and 21:00.");

        if (minutes <= 10 || minutes > 360)
            throw new IllegalArgumentException("Minutes must be between 10 and 360.");

    }

    private static boolean isValidDayOfWeek(String dayOfWeek) {
        notNull(dayOfWeek, "dayOfWeek");

        return Stream.of("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
                .anyMatch(day -> day.equals(dayOfWeek));
    }

    public LocalTime getEndTime() {
        return startTime.plusMinutes(minutes);
    }

    public boolean includes(String day, LocalTime time) {
        notNull(day, time);

        boolean isSameDay = day.equalsIgnoreCase(this.dayOfWeek);
        boolean isDuringTime = !time.isBefore(startTime) && !time.isAfter(getEndTime());

        return isSameDay && isDuringTime;
    }
}
