BEGIN;

INSERT INTO public.degrees (university_id, name, degree_type, catalog_year, total_credits_required)
SELECT university_id, 'Computer Science (B.S.) - Sample', 'major', '2026-2027', 120.00
FROM public.universities WHERE abbreviation = 'UNT';

INSERT INTO public.courses (university_id, subject, course_number, title, credits)
SELECT u.university_id, c.subject, c.course_number, c.title, c.credits
FROM public.universities u
CROSS JOIN (VALUES
    ('CSCE', '1030', 'Computer Science I',                         4.00),
    ('CSCE', '1040', 'Computer Science II',                        3.00),
    ('CSCE', '2100', 'Foundations of Computing',                   3.00),
    ('CSCE', '2110', 'Foundations of Data Structures',             3.00),
    ('CSCE', '2610', 'Assembly Language and Computer Organization', 3.00),
    ('CSCE', '3110', 'Data Structures and Algorithms',             3.00),
    ('CSCE', '3444', 'Software Engineering',                       3.00),
    ('CSCE', '3600', 'Principles of Systems Programming',          3.00),
    ('CSCE', '4110', 'Algorithms',                                 3.00),
    ('CSCE', '4600', 'Operating Systems',                          3.00),
    ('MATH', '1710', 'Calculus I',                                 4.00),
    ('MATH', '1720', 'Calculus II',                                3.00),
    ('MATH', '2700', 'Linear Algebra and Vector Geometry',         3.00),
    ('ENGL', '1310', 'College Writing I',                          3.00),
    ('ENGL', '1320', 'College Writing II',                         3.00)
) AS c(subject, course_number, title, credits)
WHERE u.abbreviation = 'UNT';

-- Prerequisites: (course, prerequisite)
INSERT INTO public.course_prerequisites (course_id, prerequisite_course_id)
SELECT c.course_id, p.course_id
FROM (VALUES
    ('CSCE', '1040', 'CSCE', '1030'),
    ('CSCE', '2100', 'CSCE', '1030'),
    ('CSCE', '2100', 'MATH', '1710'),
    ('CSCE', '2110', 'CSCE', '1040'),
    ('CSCE', '2610', 'CSCE', '1040'),
    ('CSCE', '3110', 'CSCE', '2110'),
    ('CSCE', '3110', 'CSCE', '2100'),
    ('CSCE', '3444', 'CSCE', '2110'),
    ('CSCE', '3600', 'CSCE', '2610'),
    ('CSCE', '3600', 'CSCE', '2110'),
    ('CSCE', '4110', 'CSCE', '3110'),
    ('CSCE', '4600', 'CSCE', '3600'),
    ('MATH', '1720', 'MATH', '1710'),
    ('MATH', '2700', 'MATH', '1720'),
    ('ENGL', '1320', 'ENGL', '1310')
) AS r(subject, course_number, pre_subject, pre_number)
JOIN public.universities u ON u.abbreviation = 'UNT'
JOIN public.courses c ON c.university_id = u.university_id
    AND c.subject = r.subject AND c.course_number = r.course_number
JOIN public.courses p ON p.university_id = u.university_id
    AND p.subject = r.pre_subject AND p.course_number = r.pre_number;

-- Every sample course is required by the sample degree.
INSERT INTO public.degree_requirements (degree_id, course_id, category)
SELECT d.degree_id, c.course_id,
       CASE c.subject
           WHEN 'CSCE' THEN 'Major Core'
           WHEN 'MATH' THEN 'Mathematics'
           ELSE 'Core Curriculum'
       END
FROM public.degrees d
JOIN public.courses c ON c.university_id = d.university_id
WHERE d.name = 'Computer Science (B.S.) - Sample';

COMMIT;