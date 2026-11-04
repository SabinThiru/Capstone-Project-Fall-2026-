package com.campuspath.backend.degree;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.*;

public final class DegreeDtos {

    private DegreeDtos() { }

    public record DegreeResponse(Integer id, String name, String degreeType, String catalogYear,
                                 BigDecimal totalCreditsRequired) {
        static DegreeResponse from(Degree d) {
            return new DegreeResponse(d.getId(), d.getName(), d.getDegreeType(), d.getCatalogYear(),
                    d.getTotalCreditsRequired());
        }
    }

    public record CourseResponse(Integer id, String code, String subject, String courseNumber, String title,
                                 BigDecimal credits, List<String> prerequisites) {
        static CourseResponse from(Course c) {
            return new CourseResponse(c.getId(), c.getCode(), c.getSubject(), c.getCourseNumber(), c.getTitle(),
                    c.getCredits(),
                    c.getPrerequisites().stream().map(Course::getCode).sorted(Comparator.naturalOrder()).toList());
        }
    }

    public record AddDegreeRequest(
            @NotNull(message = "Choose a degree") Integer degreeId,
            Boolean primary,
            LocalDate startDate) { }

    public record StudentDegreeResponse(Integer id, DegreeResponse degree,
                                        @JsonProperty("isPrimary") boolean isPrimary,
                                        String status, LocalDate startDate) {
        static StudentDegreeResponse from(StudentDegree sd) {
            return new StudentDegreeResponse(sd.getId(), DegreeResponse.from(sd.getDegree()), sd.isPrimary(),
                    sd.getStatus(), sd.getStartDate());
        }
    }

    private static final String STATUS_PATTERN = "completed|in_progress|planned";
    private static final String STATUS_MESSAGE = "Status must be completed, in_progress or planned";
    private static final String GRADE_PATTERN = "A|B|C|D|F|P|NP|W|I";

    public record AddCourseRequest(
            @NotNull(message = "Choose a course") Integer courseId,
            @NotBlank @Pattern(regexp = STATUS_PATTERN, message = STATUS_MESSAGE) String status,
            @Pattern(regexp = GRADE_PATTERN, message = "Grade must be A, B, C, D, F, P, NP, W or I") String grade,
            @Size(max = 30, message = "Term must be at most 30 characters") String termLabel) { }

    public record UpdateCourseRequest(
            @NotBlank @Pattern(regexp = STATUS_PATTERN, message = STATUS_MESSAGE) String status,
            @Pattern(regexp = GRADE_PATTERN, message = "Grade must be A, B, C, D, F, P, NP, W or I") String grade,
            @Size(max = 30, message = "Term must be at most 30 characters") String termLabel) { }

    public record StudentCourseResponse(Integer id, Integer courseId, String code, String title,
                                        BigDecimal credits, String status, String grade, String termLabel) {
        static StudentCourseResponse from(StudentCourse sc) {
            Course c = sc.getCourse();
            return new StudentCourseResponse(sc.getId(), c.getId(), c.getCode(), c.getTitle(), c.getCredits(),
                    sc.getStatus(), sc.getGrade(), sc.getTermLabel());
        }
    }


    public record RequirementCourse(Integer courseId, String code, String title, BigDecimal credits, String status) { }

    public record CategoryResponse(String category, int completedCount, int totalCount,
                                   List<RequirementCourse> courses) { }

    public record ProgressResponse(DegreeResponse degree, BigDecimal totalCreditsRequired,
                                   BigDecimal completedCredits, BigDecimal inProgressCredits,
                                   BigDecimal remainingCredits, int percentComplete,
                                   List<CategoryResponse> requirements) { }

    public record SuggestionResponse(Integer courseId, String code, String title, BigDecimal credits,
                                     String category, int unlocksCount, boolean recommended, String reason) { }
}