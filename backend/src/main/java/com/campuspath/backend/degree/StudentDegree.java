package com.campuspath.backend.degree;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "student_degrees", schema = "public")
public class StudentDegree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_degree_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "degree_id", nullable = false)
    private Degree degree;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryDegree;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "active";

    protected StudentDegree() { }

    public StudentDegree(Integer userId, Degree degree, boolean primary, LocalDate startDate) {
        this.userId = userId;
        this.degree = degree;
        this.primaryDegree = primary;
        this.startDate = startDate;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Degree getDegree() { return degree; }
    public boolean isPrimary() { return primaryDegree; }
    public LocalDate getStartDate() { return startDate; }
    public String getStatus() { return status; }

    public void setPrimary(boolean primary) { this.primaryDegree = primary; }
}