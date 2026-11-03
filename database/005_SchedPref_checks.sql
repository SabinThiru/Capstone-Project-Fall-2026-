SELECT
    column_name,
    data_type,
    udt_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'schedule_preferences'
  AND column_name = 'preferred_days';


SELECT *
FROM schedule_preferences
WHERE max_credits < 3
   OR max_credits > 21;


ALTER TABLE schedule_preferences
    DROP CONSTRAINT IF EXISTS chk_credit_limit;

ALTER TABLE schedule_preferences
    ADD CONSTRAINT chk_credit_limit
        CHECK (max_credits BETWEEN 3 AND 21);

SELECT
    conname,
    pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE conrelid = 'public.schedule_preferences'::regclass
  AND contype = 'c';