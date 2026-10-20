package com.campuspath.backend.university;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Integer> {

    List<University> findAllByOrderByNameAsc();
}