SELECT gs.*
FROM generated_schedules gs
         LEFT JOIN academic_terms at
                   ON gs.term_id = at.term_id
WHERE gs.term_id IS NOT NULL
  AND at.term_id IS NULL;

ALTER TABLE generated_schedules
    ADD CONSTRAINT fk_generated_schedule_term
        FOREIGN KEY (term_id)
            REFERENCES academic_terms(term_id)
            ON DELETE CASCADE;

ALTER TABLE generated_schedules
    ALTER COLUMN schedule_name TYPE VARCHAR(120);

SELECT *
FROM generated_schedules
WHERE term_id IS NULL;

ALTER TABLE generated_schedules
    ALTER COLUMN term_id SET NOT NULL;

