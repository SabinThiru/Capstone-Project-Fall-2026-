package com.campuspath.backend.degree;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DegreePlanner {

    public enum Status { COMPLETED, IN_PROGRESS, PLANNED, REMAINING }

    public record Requirement(int courseId, String code, String title, BigDecimal credits, String category) { }

    public record RequirementStatus(Requirement requirement, Status status) { }

    public record CategoryProgress(String category, int completedCount, int totalCount,
                                   List<RequirementStatus> courses) { }

    public record Progress(BigDecimal completedCredits, BigDecimal inProgressCredits,
                           BigDecimal remainingCredits, int percentComplete, List<CategoryProgress> categories) { }

    public record Suggestion(Requirement requirement, int unlocksCount, boolean recommended, String reason) { }

    private DegreePlanner() { }

    public static Progress progress(List<Requirement> requirements, Map<Integer, Status> statusByCourse,
                                    Map<Status, BigDecimal> creditsByStatus, BigDecimal totalRequired) {
        Map<String, List<RequirementStatus>> byCategory = new LinkedHashMap<>();
        for (Requirement r : sorted(requirements)) {
            Status s = statusByCourse.getOrDefault(r.courseId(), Status.REMAINING);
            byCategory.computeIfAbsent(r.category(), c -> new ArrayList<>()).add(new RequirementStatus(r, s));
        }

        List<CategoryProgress> categories = new ArrayList<>();
        byCategory.forEach((category, courses) -> {
            int done = (int) courses.stream().filter(c -> c.status() == Status.COMPLETED).count();
            categories.add(new CategoryProgress(category, done, courses.size(), courses));
        });

        BigDecimal completed = creditsByStatus.getOrDefault(Status.COMPLETED, BigDecimal.ZERO);
        BigDecimal inProgress = creditsByStatus.getOrDefault(Status.IN_PROGRESS, BigDecimal.ZERO);
        BigDecimal remaining = totalRequired.subtract(completed).max(BigDecimal.ZERO);

        int percent = 0;
        if (totalRequired.signum() > 0) {
            percent = completed.multiply(BigDecimal.valueOf(100))
                    .divide(totalRequired, 0, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100))
                    .intValue();
        }
        return new Progress(completed, inProgress, remaining, percent, categories);
    }

    public static List<Suggestion> suggest(List<Requirement> requirements, Map<Integer, Set<Integer>> prerequisites,
                                           Map<Integer, Status> statusByCourse, int maxCredits, int limit) {
        List<Requirement> remaining = requirements.stream()
                .filter(r -> {
                    Status s = statusByCourse.getOrDefault(r.courseId(), Status.REMAINING);
                    return s == Status.REMAINING || s == Status.PLANNED;
                })
                .toList();

        List<Suggestion> eligible = new ArrayList<>();
        for (Requirement r : remaining) {
            Set<Integer> needs = prerequisites.getOrDefault(r.courseId(), Set.of());
            boolean ready = needs.stream().allMatch(p -> {
                Status s = statusByCourse.getOrDefault(p, Status.REMAINING);
                return s == Status.COMPLETED || s == Status.IN_PROGRESS;
            });
            if (!ready) continue;

            int unlocks = (int) remaining.stream()
                    .filter(other -> prerequisites.getOrDefault(other.courseId(), Set.of()).contains(r.courseId()))
                    .count();
            eligible.add(new Suggestion(r, unlocks, false, reasonFor(needs.isEmpty(), unlocks)));
        }

        eligible.sort(Comparator.comparingInt(Suggestion::unlocksCount).reversed()
                .thenComparing(s -> s.requirement().code()));

        List<Suggestion> result = new ArrayList<>();
        BigDecimal load = BigDecimal.ZERO;
        BigDecimal cap = BigDecimal.valueOf(maxCredits);
        for (Suggestion s : eligible) {
            if (result.size() >= limit) break;
            BigDecimal next = load.add(s.requirement().credits());
            boolean fits = next.compareTo(cap) <= 0;
            if (fits) load = next;
            result.add(new Suggestion(s.requirement(), s.unlocksCount(), fits, s.reason()));
        }
        return result;
    }

    private static String reasonFor(boolean noPrerequisites, int unlocks) {
        String base = noPrerequisites ? "No prerequisites" : "Prerequisites done or in progress";
        if (unlocks == 0) return base;
        return base + "; unlocks " + unlocks + (unlocks == 1 ? " more required course" : " more required courses");
    }

    private static List<Requirement> sorted(List<Requirement> requirements) {
        return requirements.stream().sorted(Comparator.comparing(Requirement::code)).toList();
    }
}