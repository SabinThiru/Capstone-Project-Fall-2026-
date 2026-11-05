package com.campuspath.backend.degree;

import jakarta.persistence.*;

@Entity
@Table(name = "degree_requirements", schema = "public")
public class DegreeRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requirement_id")
    private Integer id;

    @Column(name = "degree_id", nullable = false)
    private Integer degreeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    protected DegreeRequirement() { }

    public Integer getId() { return id; }
    public Integer getDegreeId() { return degreeId; }
    public Course getCourse() { return course; }
    public String getCategory() { return category; }
}