package com.campuspath.backend.degree;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("""
            SELECT c FROM Course c
            WHERE c.universityId = :universityId
              AND (LOWER(CONCAT(c.subject, ' ', c.courseNumber)) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY c.subject, c.courseNumber
            """)
    List<Course> search(@Param("universityId") Integer universityId, @Param("search") String search);

    Optional<Course> findByIdAndUniversityId(Integer id, Integer universityId);
}