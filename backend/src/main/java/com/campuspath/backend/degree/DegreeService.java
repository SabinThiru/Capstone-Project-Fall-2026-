package com.campuspath.backend.degree;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.common.ApiException;
import com.campuspath.backend.degree.DegreeDtos.*;
import com.campuspath.backend.degree.DegreePlanner.Requirement;
import com.campuspath.backend.degree.DegreePlanner.Status;
import com.campuspath.backend.schedule.SchedulePreferences;
import com.campuspath.backend.schedule.SchedulePreferencesRepository;
import com.campuspath.backend.user.CurrentUserService;
import com.campuspath.backend.user.User;

@Service
public class DegreeService {

    private final CurrentUserService currentUser;
    private final DegreeRepository degrees;
    private final StudentDegreeRepository studentDegrees;
    private final CourseRepository courses;
    private final DegreeRequirementRepository requirements;
    private final StudentCourseRepository studentCourses;
    private final SchedulePreferencesRepository preferences;

    public DegreeService(CurrentUserService currentUser, DegreeRepository degrees,
                         StudentDegreeRepository studentDegrees, CourseRepository courses,
                         DegreeRequirementRepository requirements, StudentCourseRepository studentCourses,
                         SchedulePreferencesRepository preferences) {
        this.currentUser = currentUser;
        this.degrees = degrees;
        this.studentDegrees = studentDegrees;
        this.courses = courses;
        this.requirements = requirements;
        this.studentCourses = studentCourses;
        this.preferences = preferences;
    }

    @Transactional(readOnly = true)
    public List<DegreeResponse> listDegrees(String sub) {
        User user = currentUser.require(sub);
        return degrees.findByUniversityIdOrderByNameAscCatalogYearDesc(user.getUniversity().getId())
                .stream().map(DegreeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> searchCourses(String sub, String search) {
        User user = currentUser.require(sub);
        String term = search == null ? "" : search.trim();
        return courses.search(user.getUniversity().getId(), term).stream().map(CourseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentDegreeResponse> myDegrees(String sub) {
        return studentDegrees.findByUserIdOrderByIdAsc(currentUser.requireId(sub))
                .stream().map(StudentDegreeResponse::from).toList();
    }

    @Transactional
    public StudentDegreeResponse addDegree(String sub, AddDegreeRequest req) {
        User user = currentUser.require(sub);
        Degree degree = degrees.findByIdAndUniversityId(req.degreeId(), user.getUniversity().getId())
                .orElseThrow(() -> ApiException.badRequest("Unknown degree for your university"));
        if (studentDegrees.existsByUserIdAndDegree_Id(user.getId(), degree.getId())) {
            throw ApiException.conflict("You already added " + degree.getName());
        }

        List<StudentDegree> existing = studentDegrees.findByUserIdOrderByIdAsc(user.getId());
        boolean makePrimary = Boolean.TRUE.equals(req.primary())
                || (existing.stream().noneMatch(StudentDegree::isPrimary) && "major".equals(degree.getDegreeType()));
        if (makePrimary) clearPrimary(existing);

        StudentDegree saved = studentDegrees.save(new StudentDegree(user.getId(), degree, makePrimary, req.startDate()));
        return StudentDegreeResponse.from(saved);
    }

    @Transactional
    public StudentDegreeResponse makePrimary(String sub, Integer studentDegreeId) {
        Integer userId = currentUser.requireId(sub);
        StudentDegree chosen = studentDegrees.findByIdAndUserId(studentDegreeId, userId)
                .orElseThrow(() -> ApiException.notFound("Degree not found"));
        clearPrimary(studentDegrees.findByUserIdOrderByIdAsc(userId));
        chosen.setPrimary(true);
        return StudentDegreeResponse.from(chosen);
    }

    @Transactional
    public void removeDegree(String sub, Integer studentDegreeId) {
        Integer userId = currentUser.requireId(sub);
        StudentDegree sd = studentDegrees.findByIdAndUserId(studentDegreeId, userId)
                .orElseThrow(() -> ApiException.notFound("Degree not found"));
        studentDegrees.delete(sd);
    }

    private void clearPrimary(List<StudentDegree> all) {
        all.forEach(sd -> sd.setPrimary(false));
        studentDegrees.flush();
    }


    @Transactional(readOnly = true)
    public List<StudentCourseResponse> myCourses(String sub) {
        return studentCourses.findByUserIdOrderByIdAsc(currentUser.requireId(sub))
                .stream().map(StudentCourseResponse::from).toList();
    }

    @Transactional
    public StudentCourseResponse addCourse(String sub, AddCourseRequest req) {
        User user = currentUser.require(sub);
        Course course = courses.findByIdAndUniversityId(req.courseId(), user.getUniversity().getId())
                .orElseThrow(() -> ApiException.badRequest("Unknown course for your university"));
        if (studentCourses.existsByUserIdAndCourse_Id(user.getId(), course.getId())) {
            throw ApiException.conflict("You already added " + course.getCode());
        }
        StudentCourse sc = new StudentCourse(user.getId(), course);
        sc.update(req.status(), gradeFor(req.status(), req.grade()), blankToNull(req.termLabel()));
        return StudentCourseResponse.from(studentCourses.save(sc));
    }

    @Transactional
    public StudentCourseResponse updateCourse(String sub, Integer id, UpdateCourseRequest req) {
        StudentCourse sc = studentCourses.findByIdAndUserId(id, currentUser.requireId(sub))
                .orElseThrow(() -> ApiException.notFound("Course not found in your list"));
        sc.update(req.status(), gradeFor(req.status(), req.grade()), blankToNull(req.termLabel()));
        return StudentCourseResponse.from(sc);
    }

    @Transactional
    public void removeCourse(String sub, Integer id) {
        StudentCourse sc = studentCourses.findByIdAndUserId(id, currentUser.requireId(sub))
                .orElseThrow(() -> ApiException.notFound("Course not found in your list"));
        studentCourses.delete(sc);
    }

    private static String gradeFor(String status, String grade) {
        String g = blankToNull(grade);
        return "completed".equals(status) && g != null ? g.toUpperCase(Locale.ROOT) : null;
    }

    @Transactional(readOnly = true)
    public ProgressResponse progress(String sub) {
        Integer userId = currentUser.requireId(sub);
        Degree degree = primaryDegree(userId);
        List<StudentCourse> taken = studentCourses.findByUserIdOrderByIdAsc(userId);

        DegreePlanner.Progress p = DegreePlanner.progress(
                requirementsFor(degree), statusByCourse(taken), creditsByStatus(taken),
                degree.getTotalCreditsRequired());

        List<CategoryResponse> categories = p.categories().stream()
                .map(c -> new CategoryResponse(c.category(), c.completedCount(), c.totalCount(),
                        c.courses().stream().map(rs -> new RequirementCourse(
                                rs.requirement().courseId(), rs.requirement().code(), rs.requirement().title(),
                                rs.requirement().credits(), rs.status().name().toLowerCase(Locale.ROOT))).toList()))
                .toList();

        return new ProgressResponse(DegreeResponse.from(degree), degree.getTotalCreditsRequired(),
                p.completedCredits(), p.inProgressCredits(), p.remainingCredits(), p.percentComplete(), categories);
    }

    @Transactional(readOnly = true)
    public List<SuggestionResponse> suggestions(String sub, int limit) {
        Integer userId = currentUser.requireId(sub);
        Degree degree = primaryDegree(userId);

        List<DegreeRequirement> reqs = requirements.findByDegreeId(degree.getId());
        Map<Integer, Set<Integer>> prerequisites = new HashMap<>();
        for (DegreeRequirement r : reqs) {
            prerequisites.put(r.getCourse().getId(), r.getCourse().getPrerequisites().stream()
                    .map(Course::getId).collect(Collectors.toSet()));
        }

        int maxCredits = preferences.findByUserId(userId.longValue())
                .orElseGet(() -> SchedulePreferences.defaults(userId.longValue()))
                .getMaxCredits();

        int safeLimit = Math.max(1, Math.min(limit, 50));
        return DegreePlanner.suggest(toRequirements(reqs), prerequisites,
                        statusByCourse(studentCourses.findByUserIdOrderByIdAsc(userId)), maxCredits, safeLimit)
                .stream()
                .map(s -> new SuggestionResponse(s.requirement().courseId(), s.requirement().code(),
                        s.requirement().title(), s.requirement().credits(), s.requirement().category(),
                        s.unlocksCount(), s.recommended(), s.reason()))
                .toList();
    }


    private Degree primaryDegree(Integer userId) {
        return studentDegrees.findFirstByUserIdAndPrimaryDegreeTrueAndStatus(userId, "active")
                .map(StudentDegree::getDegree)
                .orElseThrow(() -> ApiException.notFound("Choose your major first to see degree progress"));
    }

    private List<Requirement> requirementsFor(Degree degree) {
        return toRequirements(requirements.findByDegreeId(degree.getId()));
    }

    private static List<Requirement> toRequirements(List<DegreeRequirement> reqs) {
        return reqs.stream()
                .map(r -> new Requirement(r.getCourse().getId(), r.getCourse().getCode(), r.getCourse().getTitle(),
                        r.getCourse().getCredits(), r.getCategory()))
                .toList();
    }

    private static Map<Integer, Status> statusByCourse(List<StudentCourse> taken) {
        Map<Integer, Status> map = new HashMap<>();
        for (StudentCourse sc : taken) {
            map.put(sc.getCourse().getId(), toStatus(sc.getStatus()));
        }
        return map;
    }

    private static Map<Status, BigDecimal> creditsByStatus(List<StudentCourse> taken) {
        Map<Status, BigDecimal> map = new EnumMap<>(Status.class);
        for (StudentCourse sc : taken) {
            map.merge(toStatus(sc.getStatus()), sc.getCourse().getCredits(), BigDecimal::add);
        }
        return map;
    }

    private static Status toStatus(String dbStatus) {
        return switch (dbStatus) {
            case "completed" -> Status.COMPLETED;
            case "in_progress" -> Status.IN_PROGRESS;
            case "planned" -> Status.PLANNED;
            default -> Status.REMAINING;
        };
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}