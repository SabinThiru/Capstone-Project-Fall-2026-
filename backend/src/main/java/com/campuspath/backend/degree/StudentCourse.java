package com.campuspath.backend.degree;

import jakarta.persistence.*;

@Entity
@Table(name = "student_courses", schema = "public")
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_course_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "grade", length = 2)
    private String grade;

    @Column(name = "term_label", length = 30)
    private String termLabel;

    protected StudentCourse() { }

    public StudentCourse(Integer userId, Course course) {
        this.userId = userId;
        this.course = course;
    }

    public void update(String status, String grade, String termLabel) {
        this.status = status;
        this.grade = grade;
        this.termLabel = termLabel;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Course getCourse() { return course; }
    public String getStatus() { return status; }
    public String getGrade() { return grade; }
    public String getTermLabel() { return termLabel; }
}