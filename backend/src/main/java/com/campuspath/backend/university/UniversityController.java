package com.campuspath.backend.university;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/universities")
public class UniversityController {

    private final UniversityRepository universities;

    public UniversityController(UniversityRepository universities) {
        this.universities = universities;
    }

    @GetMapping
    public List<UniversityResponse> list() {
        return universities.findAllByOrderByNameAsc()
                .stream()
                .map(UniversityResponse::from)
                .toList();
    }
}