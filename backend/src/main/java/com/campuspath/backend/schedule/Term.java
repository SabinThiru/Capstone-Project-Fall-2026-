package com.campuspath.backend.schedule;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_terms", schema = "public")
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "term_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "term_name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    protected Term() { }

    public Term(Integer userId, String name, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}