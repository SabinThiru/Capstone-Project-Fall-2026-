CREATE UNIQUE INDEX IF NOT EXISTS idx_one_selected_schedule
    ON generated_schedules(user_id, term_id)
    WHERE is_selected = TRUE;

CREATE INDEX IF NOT EXISTS idx_calendar_events_user_date
    ON calendar_events(user_id, event_date);

CREATE INDEX IF NOT EXISTS idx_commitments_user_dates
    ON commitments(user_id, start_date, end_date);

CREATE INDEX IF NOT EXISTS idx_academic_terms_user
    ON academic_terms(user_id);

CREATE INDEX IF NOT EXISTS idx_generated_schedules_user_term
    ON generated_schedules(user_id, term_id);