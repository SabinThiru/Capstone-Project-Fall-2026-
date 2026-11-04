package com.campuspath.backend.degree;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Integer> {

    @EntityGraph(attributePaths = "course")
    List<StudentCourse> findByUserIdOrderByIdAsc(Integer userId);

    @EntityGraph(attributePaths = "course")
    Optional<StudentCourse> findByIdAndUserId(Integer id, Integer userId);

    boolean existsByUserIdAndCourse_Id(Integer userId, Integer courseId);
}