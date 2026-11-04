package com.campuspath.backend.degree;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.campuspath.backend.degree.DegreeDtos.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class DegreeController {

    private final DegreeService degrees;

    public DegreeController(DegreeService degrees) {
        this.degrees = degrees;
    }

    @GetMapping("/degrees")
    public List<DegreeResponse> listDegrees(@AuthenticationPrincipal Jwt jwt) {
        return degrees.listDegrees(jwt.getSubject());
    }

    // ?search=CSCE or ?search=calculus (leave out to list all)
    @GetMapping("/courses")
    public List<CourseResponse> searchCourses(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(required = false) String search) {
        return degrees.searchCourses(jwt.getSubject(), search);
    }

    @GetMapping("/users/me/degrees")
    public List<StudentDegreeResponse> myDegrees(@AuthenticationPrincipal Jwt jwt) {
        return degrees.myDegrees(jwt.getSubject());
    }

    @PostMapping("/users/me/degrees")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDegreeResponse addDegree(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddDegreeRequest body) {
        return degrees.addDegree(jwt.getSubject(), body);
    }

    @PostMapping("/users/me/degrees/{id}/primary")
    public StudentDegreeResponse makePrimary(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return degrees.makePrimary(jwt.getSubject(), id);
    }

    @DeleteMapping("/users/me/degrees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDegree(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        degrees.removeDegree(jwt.getSubject(), id);
    }

    @GetMapping("/users/me/courses")
    public List<StudentCourseResponse> myCourses(@AuthenticationPrincipal Jwt jwt) {
        return degrees.myCourses(jwt.getSubject());
    }

    @PostMapping("/users/me/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentCourseResponse addCourse(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCourseRequest body) {
        return degrees.addCourse(jwt.getSubject(), body);
    }

    @PutMapping("/users/me/courses/{id}")
    public StudentCourseResponse updateCourse(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                              @Valid @RequestBody UpdateCourseRequest body) {
        return degrees.updateCourse(jwt.getSubject(), id, body);
    }

    @DeleteMapping("/users/me/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCourse(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        degrees.removeCourse(jwt.getSubject(), id);
    }

    @GetMapping("/users/me/degree-progress")
    public ProgressResponse progress(@AuthenticationPrincipal Jwt jwt) {
        return degrees.progress(jwt.getSubject());
    }

    @GetMapping("/users/me/course-suggestions")
    public List<SuggestionResponse> suggestions(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(defaultValue = "8") int limit) {
        return degrees.suggestions(jwt.getSubject(), limit);
    }
}