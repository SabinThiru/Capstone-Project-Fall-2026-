package com.campuspath.backend.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReminderPlanner {

    public record Occurrence(String sourceType, long sourceId, String title, LocalDate date,
                             LocalTime start, String location) { }

    public record DueReminder(String sourceType, String sourceKey, String title, String message, Instant notifyAt) { }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US);

    private ReminderPlanner() { }

    public static List<DueReminder> due(List<Occurrence> occurrences, ZoneId zone, Instant now, int minutesBefore) {
        List<DueReminder> result = new ArrayList<>();
        LocalDate today = now.atZone(zone).toLocalDate();

        for (Occurrence o : occurrences) {
            Instant start = o.date().atTime(o.start()).atZone(zone).toInstant();
            Instant notifyAt = start.minusSeconds(minutesBefore * 60L);
            if (notifyAt.isAfter(now) || !now.isBefore(start)) continue;

            String when = o.date().equals(today)
                    ? "Today at " + TIME.format(o.start())
                    : DAY.format(o.date()) + " at " + TIME.format(o.start());
            String message = (o.location() == null || o.location().isBlank())
                    ? when
                    : when + " · " + o.location();

            String key = o.sourceId() + ":" + o.date();
            result.add(new DueReminder(o.sourceType(), key, "Upcoming: " + o.title(), message, notifyAt));
        }
        return result;
    }
}