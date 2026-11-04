BEGIN;

-- 1. COURSE CATALOG (R3, R4)
CREATE TABLE public.courses (
    course_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    university_id INT           NOT NULL,
    subject       VARCHAR(10)   NOT NULL,   -- e.g. CSCE
    course_number VARCHAR(10)   NOT NULL,   -- e.g. 1030
    title         VARCHAR(200)  NOT NULL,
    credits       DECIMAL(4, 2) NOT NULL,
    description   VARCHAR(2000),

    CONSTRAINT fk_courses_university FOREIGN KEY (university_id)
        REFERENCES public.universities (university_id),
    CONSTRAINT uq_courses_code UNIQUE (university_id, subject, course_number),
    CONSTRAINT chk_courses_credits CHECK (credits >= 0)
);


-- 2. PREREQUISITES: every listed prerequisite must be completed (AND).
CREATE TABLE public.course_prerequisites (
    course_id              INT NOT NULL,
    prerequisite_course_id INT NOT NULL,

    PRIMARY KEY (course_id, prerequisite_course_id),
    CONSTRAINT fk_course_prerequisites_course FOREIGN KEY (course_id)
        REFERENCES public.courses (course_id) ON DELETE CASCADE,
    CONSTRAINT fk_course_prerequisites_prereq FOREIGN KEY (prerequisite_course_id)
        REFERENCES public.courses (course_id) ON DELETE CASCADE,
    CONSTRAINT chk_course_prerequisites_not_self CHECK (course_id <> prerequisite_course_id)
);


-- 3. COURSES REQUIRED BY EACH DEGREE (R3)
CREATE TABLE public.degree_requirements (
    requirement_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    degree_id      INT         NOT NULL,
    course_id      INT         NOT NULL,
    category       VARCHAR(50) NOT NULL DEFAULT 'Major Requirements',

    CONSTRAINT fk_degree_requirements_degree FOREIGN KEY (degree_id)
        REFERENCES public.degrees (degree_id) ON DELETE CASCADE,
    CONSTRAINT fk_degree_requirements_course FOREIGN KEY (course_id)
        REFERENCES public.courses (course_id),
    CONSTRAINT uq_degree_requirements UNIQUE (degree_id, course_id)
);


-- 4. COURSES A STUDENT HAS TAKEN OR IS TAKING (R3, R4)
CREATE TABLE public.student_courses (
    student_course_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           INT         NOT NULL,
    course_id         INT         NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'completed',
    grade             VARCHAR(2),
    term_label        VARCHAR(30),   -- e.g. "Fall 2025"

    CONSTRAINT fk_student_courses_user FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_courses_course FOREIGN KEY (course_id)
        REFERENCES public.courses (course_id),
    CONSTRAINT uq_student_courses UNIQUE (user_id, course_id),
    CONSTRAINT chk_student_courses_status CHECK (status IN ('completed', 'in_progress', 'planned'))
);


-- 5. NOTIFICATIONS / REMINDERS shown in the bell (R2)
CREATE TABLE public.notifications (
    notification_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         INT          NOT NULL,
    title           VARCHAR(200) NOT NULL,
    message         VARCHAR(500),
    notify_at       TIMESTAMPTZ  NOT NULL,
    is_read         BOOLEAN      NOT NULL DEFAULT FALSE,
    source_type     VARCHAR(20)  NOT NULL DEFAULT 'system',
    source_key      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
        REFERENCES public.users (user_id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_source CHECK (source_type IN ('event', 'commitment', 'system')),
    -- Stops the same reminder from being created twice.
    CONSTRAINT uq_notifications_source UNIQUE (user_id, source_type, source_key)
);

CREATE INDEX idx_notifications_user_time ON public.notifications (user_id, notify_at DESC);

COMMIT;