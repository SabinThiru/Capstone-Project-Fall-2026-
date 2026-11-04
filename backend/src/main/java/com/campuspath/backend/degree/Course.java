package com.campuspath.backend.degree;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
@Table(name = "courses", schema = "public")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer id;

    @Column(name = "university_id", nullable = false)
    private Integer universityId;

    @Column(name = "subject", nullable = false, length = 10)
    private String subject;

    @Column(name = "course_number", nullable = false, length = 10)
    private String courseNumber;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "credits", nullable = false, precision = 4, scale = 2)
    private BigDecimal credits;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_prerequisites", schema = "public",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_course_id"))
    private Set<Course> prerequisites = new HashSet<>();

    protected Course() { }

    public String getCode() { return subject + " " + courseNumber; }

    public Integer getId() { return id; }
    public Integer getUniversityId() { return universityId; }
    public String getSubject() { return subject; }
    public String getCourseNumber() { return courseNumber; }
    public String getTitle() { return title; }
    public BigDecimal getCredits() { return credits; }
    public String getDescription() { return description; }
    public Set<Course> getPrerequisites() { return prerequisites; }
}