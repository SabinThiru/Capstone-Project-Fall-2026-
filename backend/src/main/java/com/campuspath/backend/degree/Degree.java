package com.campuspath.backend.degree;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "degrees", schema = "public")
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "degree_id")
    private Integer id;

    @Column(name = "university_id", nullable = false)
    private Integer universityId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "degree_type", nullable = false, length = 20)
    private String degreeType;

    @Column(name = "catalog_year", nullable = false, length = 9)
    private String catalogYear;

    @Column(name = "total_credits_required", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalCreditsRequired;

    protected Degree() { }

    public Integer getId() { return id; }
    public Integer getUniversityId() { return universityId; }
    public String getName() { return name; }
    public String getDegreeType() { return degreeType; }
    public String getCatalogYear() { return catalogYear; }
    public BigDecimal getTotalCreditsRequired() { return totalCreditsRequired; }
}