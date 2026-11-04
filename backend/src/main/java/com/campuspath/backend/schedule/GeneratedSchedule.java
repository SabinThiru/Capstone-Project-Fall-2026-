package com.campuspath.backend.schedule;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "generated_schedules", schema = "public")
public class GeneratedSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "schedule_name", nullable = false, length = 120)
    private String scheduleName;

    @Column(name = "score", precision = 6, scale = 2)
    private BigDecimal score;

    @Column(name = "is_selected")
    private Boolean selected = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected GeneratedSchedule() { }

    public GeneratedSchedule(Long userId, Long termId, String scheduleName, BigDecimal score) {
        this.userId = userId;
        this.termId = termId;
        this.scheduleName = scheduleName;
        this.score = score;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getTermId() { return termId; }
    public String getScheduleName() { return scheduleName; }
    public BigDecimal getScore() { return score; }
    public boolean isSelected() { return Boolean.TRUE.equals(selected); }
    public Instant getCreatedAt() { return createdAt; }

    public void setSelected(boolean selected) { this.selected = selected; }
}