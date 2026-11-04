package com.campuspath.backend.degree;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentDegreeRepository extends JpaRepository<StudentDegree, Integer> {

    @EntityGraph(attributePaths = "degree")
    List<StudentDegree> findByUserIdOrderByIdAsc(Integer userId);

    @EntityGraph(attributePaths = "degree")
    Optional<StudentDegree> findFirstByUserIdAndPrimaryDegreeTrueAndStatus(Integer userId, String status);

    Optional<StudentDegree> findByIdAndUserId(Integer id, Integer userId);

    boolean existsByUserIdAndDegree_Id(Integer userId, Integer degreeId);
}