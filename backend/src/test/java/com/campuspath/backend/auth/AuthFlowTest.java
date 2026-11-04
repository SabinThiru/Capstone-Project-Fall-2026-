package com.campuspath.backend.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.campuspath.backend.common.ApiException;

import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    CognitoAuthService cognito;

    private static RequestPostProcessor loggedInAs(String sub, String email) {
        return jwt().jwt(j -> j.subject(sub).claim("email", email));
    }

    private static final String CHANGE_BODY = """
            {"currentPassword": "OldPass!1", "newPassword": "NewPass!2"}
            """;

    @Test
    void changePasswordUsesEmailFromToken() throws Exception {
        mvc.perform(post("/api/auth/change-password").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON).content(CHANGE_BODY))
                .andExpect(status().isNoContent());

        verify(cognito).changePassword("test@my.unt.edu", "OldPass!1", "NewPass!2");
    }

    @Test
    void changePasswordWithoutTokenIs401() throws Exception {
        mvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON).content(CHANGE_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shortNewPasswordIs400() throws Exception {
        mvc.perform(post("/api/auth/change-password").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "OldPass!1", "newPassword": "short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());

        verify(cognito, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void samePasswordIs400() throws Exception {
        mvc.perform(post("/api/auth/change-password").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "OldPass!1", "newPassword": "OldPass!1"}
                                """))
                .andExpect(status().isBadRequest());

        verify(cognito, never()).changePassword(anyString(), anyString(), anyString());
    }

    @Test
    void wrongCurrentPasswordIs400WithMessage() throws Exception {
        doThrow(ApiException.badRequest("Your current password is incorrect."))
                .when(cognito).changePassword("test@my.unt.edu", "OldPass!1", "NewPass!2");

        mvc.perform(post("/api/auth/change-password").with(loggedInAs("sub-1", "test@my.unt.edu"))
                        .contentType(MediaType.APPLICATION_JSON).content(CHANGE_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Your current password is incorrect."));
    }

    @Test
    void loginWithWrongPasswordIs401NotServerError() throws Exception {
        when(cognito.login(anyString(), anyString()))
                .thenThrow(NotAuthorizedException.builder().message("Incorrect username or password.").build());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@my.unt.edu", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Incorrect email or password."));
    }

    @Test
    void registeringExistingEmailIs409() throws Exception {
        when(cognito.register(anyString(), anyString(), anyString()))
                .thenThrow(UsernameExistsException.builder().message("User already exists").build());

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "test@my.unt.edu", "password": "Password!1", "fullName": "Test Student"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("An account with this email already exists."));
    }
}