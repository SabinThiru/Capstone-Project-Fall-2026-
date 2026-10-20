package com.campuspath.backend.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserFlowTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    private static RequestPostProcessor loggedInAs(String sub, String email) {
        return jwt().jwt(j -> j.subject(sub).claim("email", email));
    }

    private static final String VALID_BODY = """
            {"universityId": 1, "fullName": "Test Student", "euid": "ts1234"}
            """;

    @Test
    void noTokenIs401() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notRegisteredYetIs404() throws Exception {
        mvc.perform(get("/api/users/me").with(loggedInAs("sub-new", "new@my.unt.edu")))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerThenGetProfile() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "Test@My.UNT.edu"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@my.unt.edu"))
                .andExpect(jsonPath("$.university.abbreviation").value("UNT"));

        mvc.perform(get("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test Student"));
    }

    @Test
    void registeringTwiceIs409() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void sameEuidDifferentPersonIs409() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "a@my.unt.edu"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/users/me").with(loggedInAs("sub-2", "b@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void missingFieldsIs400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"universityId": 1, "fullName": "", "euid": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.euid").exists());
    }

    @Test
    void unknownUniversityIs400() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"universityId": 999, "fullName": "Test", "euid": "x1"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void settingsDefaultsThenUpdate() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/users/me/settings").with(loggedInAs("sub-1", "test@my.unt.edu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("system"))
                .andExpect(jsonPath("$.defaultReminderMinutes").value(15));

        mvc.perform(put("/api/users/me/settings").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timezone":"America/Chicago","theme":"dark","notificationsEnabled":true,
                                 "emailRemindersEnabled":false,"defaultReminderMinutes":30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));
    }

    @Test
    void invalidThemeIs400() throws Exception {
        mvc.perform(post("/api/users/me").with(loggedInAs("sub-1", "test@my.unt.edu"))
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated());

        mvc.perform(put("/api/users/me/settings").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timezone":"America/Chicago","theme":"purple","notificationsEnabled":true,
                                 "emailRemindersEnabled":true,"defaultReminderMinutes":15}
                                """))
                .andExpect(status().isBadRequest());
    }
}