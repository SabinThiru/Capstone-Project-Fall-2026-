package com.campuspath.backend.degree;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreeRepository extends JpaRepository<Degree, Integer> {

    List<Degree> findByUniversityIdOrderByNameAscCatalogYearDesc(Integer universityId);

    Optional<Degree> findByIdAndUniversityId(Integer id, Integer universityId);
}