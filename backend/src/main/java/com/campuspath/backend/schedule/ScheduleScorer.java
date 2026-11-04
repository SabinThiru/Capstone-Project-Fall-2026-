package com.campuspath.backend.schedule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class ScheduleScorer {

    public record Slot(String type, int dayOfWeek, LocalTime start, LocalTime end) { }

    public record Preferences(LocalTime earliest, LocalTime latest, Set<Integer> preferredDays,
                              boolean minimizeGaps, boolean minimizeCampusDays) { }

    private ScheduleScorer() { }

    public static BigDecimal score(List<Slot> slots, Preferences prefs) {
        List<Slot> classes = slots.stream().filter(s -> "class".equals(s.type())).toList();
        if (classes.isEmpty()) return null;

        double score = 100;

        Map<Integer, List<Slot>> byDay = groupByDay(slots);
        for (List<Slot> day : byDay.values()) {
            for (int i = 0; i < day.size(); i++) {
                for (int j = i + 1; j < day.size(); j++) {
                    if (overlaps(day.get(i), day.get(j))) score -= 20;
                }
            }
        }

        for (Slot c : classes) {
            if (c.start().isBefore(prefs.earliest()) || c.end().isAfter(prefs.latest())) score -= 10;
            if (!prefs.preferredDays().isEmpty() && !prefs.preferredDays().contains(c.dayOfWeek())) score -= 10;
        }

        Map<Integer, List<Slot>> classDays = groupByDay(classes);

        if (prefs.minimizeGaps()) {
            long gapMinutes = 0;
            for (List<Slot> day : classDays.values()) {
                for (int i = 1; i < day.size(); i++) {
                    LocalTime previousEnd = day.get(i - 1).end();
                    LocalTime nextStart = day.get(i).start();
                    if (nextStart.isAfter(previousEnd)) {
                        gapMinutes += Duration.between(previousEnd, nextStart).toMinutes();
                    }
                }
            }
            score -= gapMinutes / 15;
        }

        if (prefs.minimizeCampusDays() && classDays.size() > 3) {
            score -= 5 * (classDays.size() - 3);
        }

        score = Math.max(0, Math.min(100, score));
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    static boolean overlaps(Slot a, Slot b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    private static Map<Integer, List<Slot>> groupByDay(List<Slot> slots) {
        Map<Integer, List<Slot>> byDay = new TreeMap<>();
        for (Slot s : slots) {
            byDay.computeIfAbsent(s.dayOfWeek(), d -> new ArrayList<>()).add(s);
        }
        byDay.values().forEach(list -> list.sort(Comparator.comparing(Slot::start)));
        return byDay;
    }
}