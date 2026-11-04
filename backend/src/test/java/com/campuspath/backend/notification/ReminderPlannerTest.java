package com.campuspath.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.campuspath.backend.notification.ReminderPlanner.DueReminder;
import com.campuspath.backend.notification.ReminderPlanner.Occurrence;

class ReminderPlannerTest {

    private static final ZoneId CHICAGO = ZoneId.of("America/Chicago");
    private static final LocalDate DAY = LocalDate.of(2026, 11, 3);
    private static final Occurrence CLASS_AT_10 =
            new Occurrence("commitment", 7, "CSCE 3444", DAY, LocalTime.of(10, 0), "NTDP B190");

    private static Instant at(int hour, int minute) {
        return DAY.atTime(hour, minute).atZone(CHICAGO).toInstant();
    }

    @Test
    void notDueBeforeTheReminderWindow() {
        assertThat(ReminderPlanner.due(List.of(CLASS_AT_10), CHICAGO, at(9, 44), 15)).isEmpty();
    }

    @Test
    void dueInsideTheReminderWindow() {
        List<DueReminder> due = ReminderPlanner.due(List.of(CLASS_AT_10), CHICAGO, at(9, 50), 15);
        assertThat(due).hasSize(1);
        assertThat(due.get(0).title()).isEqualTo("Upcoming: CSCE 3444");
        assertThat(due.get(0).message()).isEqualTo("Today at 10:00 AM · NTDP B190");
        assertThat(due.get(0).sourceKey()).isEqualTo("7:2026-11-03");
        assertThat(due.get(0).notifyAt()).isEqualTo(at(9, 45));
    }

    @Test
    void notCreatedOnceItHasStarted() {
        assertThat(ReminderPlanner.due(List.of(CLASS_AT_10), CHICAGO, at(10, 0), 15)).isEmpty();
    }
}