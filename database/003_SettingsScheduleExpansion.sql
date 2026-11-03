CREATE TABLE schedule_preferences
(
    preference_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE,
    earliest_class_time  TIME       DEFAULT '08:00:00',
    latest_class_time    TIME       DEFAULT '20:00:00',
    max_credits          INTEGER    DEFAULT 15,
    preferred_days       SMALLINT[] DEFAULT ARRAY [1,2,3,4,5]::SMALLINT[],
    minimize_gaps        BOOLEAN    DEFAULT FALSE,
    minimize_campus_days BOOLEAN    DEFAULT FALSE,

    CONSTRAINT fk_preferences_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_credit_limit
        CHECK (max_credits BETWEEN 1 AND 30),

    CONSTRAINT chk_class_times
        CHECK (earliest_class_time < latest_class_time),

    CONSTRAINT chk_preferred_days
        CHECK (
            preferred_days IS NULL
                OR preferred_days <@ ARRAY [1,2,3,4,5,6,7]::SMALLINT[]
)
    );

CREATE TABLE commitments
(
    commitment_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    title           VARCHAR(150) NOT NULL,
    commitment_type VARCHAR(30)  NOT NULL,
    day_of_week     SMALLINT,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE,
    color           VARCHAR(7) DEFAULT '#4287F5',

    CONSTRAINT fk_commitments_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_commitment_type
        CHECK (commitment_type IN (
                                   'work',
                                   'personal',
                                   'extracurricular',
                                   'other'
            )),

    CONSTRAINT chk_day_of_week
        CHECK (day_of_week BETWEEN 1 AND 7),

    CONSTRAINT chk_commitment_times
        CHECK (start_time < end_time),

    CONSTRAINT chk_commitment_dates
        CHECK (end_date IS NULL OR end_date >= start_date),

    CONSTRAINT chk_commitment_color
        CHECK (color ~ '^#[0-9A-Fa-f]{6}$')
    );

CREATE TABLE generated_schedules
(
    schedule_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    term_id       BIGINT,
    schedule_name VARCHAR(100) NOT NULL,
    score         NUMERIC(6, 2),
    is_selected   BOOLEAN     DEFAULT FALSE,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_generated_schedule_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_commitments_user_id
    ON commitments(user_id);

CREATE INDEX idx_generated_schedules_user_id
    ON generated_schedules(user_id);

CREATE INDEX idx_generated_schedules_term_id
    ON generated_schedules(term_id);