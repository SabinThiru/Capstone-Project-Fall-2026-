package com.campuspath.backend.degree;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.campuspath.backend.degree.DegreePlanner.Requirement;
import com.campuspath.backend.degree.DegreePlanner.Status;
import com.campuspath.backend.degree.DegreePlanner.Suggestion;

class DegreePlannerTest {

    private static final Requirement CS1 = req(1, "CSCE 1030", 4, "Major Core");
    private static final Requirement CS2 = req(2, "CSCE 1040", 3, "Major Core");
    private static final Requirement DS = req(3, "CSCE 2110", 3, "Major Core");
    private static final Requirement CALC1 = req(4, "MATH 1710", 4, "Mathematics");
    private static final Requirement CALC2 = req(5, "MATH 1720", 3, "Mathematics");
    private static final List<Requirement> ALL = List.of(CS1, CS2, DS, CALC1, CALC2);
    private static final Map<Integer, Set<Integer>> PREREQS = Map.of(2, Set.of(1), 3, Set.of(2), 5, Set.of(4));

    private static Requirement req(int id, String code, int credits, String category) {
        return new Requirement(id, code, code + " title", BigDecimal.valueOf(credits), category);
    }

    @Test
    void progressCountsCompletedCreditsAndGroupsByCategory() {
        DegreePlanner.Progress p = DegreePlanner.progress(ALL,
                Map.of(1, Status.COMPLETED, 4, Status.IN_PROGRESS),
                Map.of(Status.COMPLETED, BigDecimal.valueOf(4), Status.IN_PROGRESS, BigDecimal.valueOf(4)),
                BigDecimal.valueOf(120));

        assertThat(p.completedCredits()).isEqualByComparingTo("4");
        assertThat(p.remainingCredits()).isEqualByComparingTo("116");
        assertThat(p.percentComplete()).isEqualTo(3);
        assertThat(p.categories()).extracting(DegreePlanner.CategoryProgress::category)
                .containsExactly("Major Core", "Mathematics");
        assertThat(p.categories().get(0).completedCount()).isEqualTo(1);
        assertThat(p.categories().get(0).totalCount()).isEqualTo(3);
    }

    @Test
    void newStudentIsSuggestedCoursesWithNoPrerequisitesFirst() {
        List<Suggestion> s = DegreePlanner.suggest(ALL, PREREQS, Map.of(), 15, 10);
        assertThat(s).extracting(x -> x.requirement().code()).containsExactly("CSCE 1030", "MATH 1710");
        assertThat(s).allMatch(Suggestion::recommended);
    }

    @Test
    void finishingAPrerequisiteUnlocksTheNextCourse() {
        List<Suggestion> s = DegreePlanner.suggest(ALL, PREREQS,
                Map.of(1, Status.COMPLETED, 4, Status.IN_PROGRESS), 15, 10);
        // CS2 (unlocks DS) ranks above CALC2 (unlocks nothing)
        assertThat(s).extracting(x -> x.requirement().code()).containsExactly("CSCE 1040", "MATH 1720");
        assertThat(s.get(0).unlocksCount()).isEqualTo(1);
    }

    @Test
    void onlyCoursesThatFitMaxCreditsAreRecommended() {
        List<Suggestion> s = DegreePlanner.suggest(ALL, PREREQS, Map.of(), 4, 10);
        assertThat(s.get(0).recommended()).isTrue();
        assertThat(s.get(1).recommended()).isFalse();
    }
}