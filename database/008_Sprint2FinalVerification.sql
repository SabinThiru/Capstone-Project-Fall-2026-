SELECT table_name,
       column_name,
       data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN (
                     'user_settings',
                     'schedule_preferences',
                     'commitments',
                     'generated_schedules',
                     'academic_terms',
                     'calendar_events'
    )
ORDER BY table_name, ordinal_position;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN (
                     'academic_terms',
                     'calendar_events'
    );

SELECT tc.table_name,
       kcu.column_name,
       ccu.table_name  AS referenced_table,
       ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
         JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
                  AND tc.table_schema = kcu.table_schema
         JOIN information_schema.constraint_column_usage ccu
              ON tc.constraint_name = ccu.constraint_name
                  AND tc.table_schema = ccu.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
  AND tc.table_name IN (
                        'user_settings',
                        'schedule_preferences',
                        'commitments',
                        'generated_schedules',
                        'academic_terms',
                        'calendar_events'
    )
ORDER BY tc.table_name;

SELECT * FROM commitments;

SELECT * FROM calendar_events;

SELECT * FROM academic_terms;

SELECT * FROM generated_schedules;

SELECT * FROM schedule_preferences;

SELECT * FROM user_settings;