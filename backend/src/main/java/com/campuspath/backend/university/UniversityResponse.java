package com.campuspath.backend.university;

public record UniversityResponse(Integer id, String name, String abbreviation) {

    public static UniversityResponse from(University u) {
        return new UniversityResponse(u.getId(), u.getName(), u.getAbbreviation());
    }
}