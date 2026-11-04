package com.campuspath.backend.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.campuspath.backend.schedule.ScheduleScorer.Preferences;
import com.campuspath.backend.schedule.ScheduleScorer.Slot;

class ScheduleScorerTest {

    private static final Preferences WEEKDAYS_8_TO_6 =
            new Preferences(LocalTime.of(8, 0), LocalTime.of(18, 0), Set.of(1, 2, 3, 4, 5), false, false);

    private static Slot cls(int day, int startHour, int endHour) {
        return new Slot("class", day, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0));
    }

    @Test
    void noClassesMeansNoScore() {
        assertThat(ScheduleScorer.score(List.of(new Slot("work", 1, LocalTime.of(9, 0), LocalTime.of(12, 0))),
                WEEKDAYS_8_TO_6)).isNull();
    }

    @Test
    void perfectScheduleScores100() {
        assertThat(ScheduleScorer.score(List.of(cls(1, 9, 10), cls(3, 9, 10)), WEEKDAYS_8_TO_6))
                .isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void conflictWithWorkCosts20() {
        List<Slot> slots = List.of(cls(1, 9, 11), new Slot("work", 1, LocalTime.of(10, 0), LocalTime.of(14, 0)));
        assertThat(ScheduleScorer.score(slots, WEEKDAYS_8_TO_6)).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    void earlyClassAndSaturdayClassEachCost10() {
        List<Slot> slots = List.of(cls(1, 7, 8), cls(6, 10, 11));
        assertThat(ScheduleScorer.score(slots, WEEKDAYS_8_TO_6)).isEqualByComparingTo(BigDecimal.valueOf(80));
    }

    @Test
    void gapsCostPointsOnlyWhenMinimizeGapsIsOn() {
        Preferences gaps = new Preferences(LocalTime.of(8, 0), LocalTime.of(18, 0), Set.of(1, 2, 3, 4, 5), true, false);
        List<Slot> slots = List.of(cls(1, 9, 10), cls(1, 13, 14));   // 3-hour gap = 12 x 15 min
        assertThat(ScheduleScorer.score(slots, WEEKDAYS_8_TO_6)).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(ScheduleScorer.score(slots, gaps)).isEqualByComparingTo(BigDecimal.valueOf(88));
    }
}