package com.campuspath.backend.degree;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreeRequirementRepository extends JpaRepository<DegreeRequirement, Integer> {

    @EntityGraph(attributePaths = {"course", "course.prerequisites"})
    List<DegreeRequirement> findByDegreeId(Integer degreeId);
}