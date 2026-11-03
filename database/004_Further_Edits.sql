ALTER TABLE user_settings
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ
        DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE academic_terms
(
    term_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id    INTEGER      NOT NULL,

    term_name  VARCHAR(100) NOT NULL,

    start_date DATE         NOT NULL,

    end_date   DATE         NOT NULL,

    CONSTRAINT fk_academic_terms_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_academic_term_dates
        CHECK (end_date >= start_date),

    CONSTRAINT uq_user_academic_term
        UNIQUE (user_id, term_name)
);

CREATE TABLE calendar_events
(
    event_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    user_id    INTEGER      NOT NULL,

    title      VARCHAR(160) NOT NULL,

    event_type VARCHAR(30)  NOT NULL,

    event_date DATE         NOT NULL,

    start_time TIME         NOT NULL,

    end_time   TIME         NOT NULL,

    location   VARCHAR(180),

    notes      VARCHAR(500),

    CONSTRAINT fk_calendar_events_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_calendar_event_type
        CHECK (
            event_type IN ('class', 'work', 'personal')
            ),

    CONSTRAINT chk_calendar_event_times
        CHECK (end_time > start_time)
);

ALTER TABLE commitments
    DROP CONSTRAINT IF EXISTS chk_commitment_type;

ALTER TABLE commitments
    ADD CONSTRAINT chk_commitment_type
        CHECK (
            commitment_type IN (
                                'class',
                                'work',
                                'personal',
                                'extracurricular',
                                'other'
                )
            );

ALTER TABLE commitments
    ALTER COLUMN title TYPE VARCHAR(160);