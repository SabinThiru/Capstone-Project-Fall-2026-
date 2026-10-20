package com.campuspath.backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    @EntityGraph(attributePaths = "university")
    Optional<User> findByCognitoSub(String cognitoSub);

    boolean existsByCognitoSub(String cognitoSub);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUniversity_IdAndEuid(Integer universityId, String euid);
}