package com.campuspath.backend.schedule;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByUserIdOrderByStartDateDesc(Integer userId);

    Optional<Term> findByIdAndUserId(Long id, Integer userId);

    boolean existsByUserIdAndNameIgnoreCase(Integer userId, String name);
}