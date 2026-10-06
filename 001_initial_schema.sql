BEGIN;

-- 1. UNIVERSITIES
CREATE TABLE public.universities (
                                     university_id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     name                  VARCHAR(200) NOT NULL,
                                     abbreviation          VARCHAR(20),
                                     city                  VARCHAR(100),
                                     state_province        VARCHAR(100),
                                     country_code          CHAR(2),
                                     timezone              VARCHAR(50)  NOT NULL,
                                     website_url           VARCHAR(500),
                                     catalog_url           VARCHAR(500),
                                     academic_calendar_url VARCHAR(500),
                                     created_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 2. USERS
CREATE TABLE public.users (
                              user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              university_id INT NOT NULL,
                              cognito_sub VARCHAR(128) NOT NULL UNIQUE,
                              email VARCHAR(254) NOT NULL,
                              full_name VARCHAR(200) NOT NULL,
                              euid VARCHAR(50) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_users_university
                                  FOREIGN KEY (university_id)
                                      REFERENCES public.universities (university_id),

                              CONSTRAINT uq_users_university_euid
                                  UNIQUE (university_id, euid),

                              CONSTRAINT chk_users_email_not_blank
                                  CHECK (BTRIM(email) <> ''),

                              CONSTRAINT chk_users_name_not_blank
                                  CHECK (BTRIM(full_name) <> ''),

                              CONSTRAINT chk_users_euid_not_blank
                                  CHECK (BTRIM(euid) <> '')
);

-- Prevent duplicate emails differing only in capitalization or spaces.
CREATE UNIQUE INDEX uq_users_email
    ON public.users (LOWER(BTRIM(email)));


-- 3. USER SETTINGS
CREATE TABLE public.user_settings (
                                      user_id INT PRIMARY KEY,
                                      timezone VARCHAR(50) NOT NULL DEFAULT 'America/Chicago',
                                      theme VARCHAR(20) NOT NULL DEFAULT 'system',
                                      notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                      email_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                      default_reminder_minutes INT NOT NULL DEFAULT 15,

                                      CONSTRAINT fk_user_settings_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES public.users (user_id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT chk_user_settings_theme
                                          CHECK (theme IN ('light', 'dark', 'system')),

                                      CONSTRAINT chk_user_settings_reminder
                                          CHECK (default_reminder_minutes >= 0)
);


-- 4. DEGREES
CREATE TABLE public.degrees (
                                degree_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                university_id  INT NOT NULL,
                                name  VARCHAR(200)  NOT NULL,
                                degree_type VARCHAR(20)   NOT NULL,
                                catalog_year VARCHAR(9)    NOT NULL,
                                total_credits_required DECIMAL(5, 2) NOT NULL,

                                CONSTRAINT fk_degrees_university
                                    FOREIGN KEY (university_id)
                                        REFERENCES public.universities (university_id),

                                CONSTRAINT chk_degrees_type
                                    CHECK (degree_type IN ('major', 'minor')),

                                CONSTRAINT chk_degrees_credits
                                    CHECK (total_credits_required > 0),

                                CONSTRAINT uq_degrees_catalog_entry
                                    UNIQUE (university_id, name, degree_type, catalog_year)
);


-- 5. STUDENT DEGREES
CREATE TABLE public.student_degrees (
                                        student_degree_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        user_id INT NOT NULL,
                                        degree_id INT NOT NULL,
                                        is_primary BOOLEAN NOT NULL DEFAULT FALSE,
                                        start_date DATE,
                                        status VARCHAR(20) NOT NULL DEFAULT 'active',

                                        CONSTRAINT fk_student_degrees_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES public.users (user_id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_student_degrees_degree
                                            FOREIGN KEY (degree_id)
                                                REFERENCES public.degrees (degree_id),

                                        CONSTRAINT uq_student_degrees_assignment
                                            UNIQUE (user_id, degree_id),

                                        CONSTRAINT chk_student_degrees_status
                                            CHECK (status IN ('active', 'completed', 'discontinued'))
);

-- A student can have at most one active primary degree.
CREATE UNIQUE INDEX uq_student_degrees_active_primary
    ON public.student_degrees (user_id)
    WHERE is_primary = TRUE AND status = 'active';

-- Support finding students assigned to a particular degree.
CREATE INDEX idx_student_degrees_degree
    ON public.student_degrees (degree_id);


-- Automatically update updated_at when a row changes.
CREATE FUNCTION public.set_updated_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_universities_updated_at
    BEFORE UPDATE ON public.universities
    FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON public.users
    FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

COMMIT;