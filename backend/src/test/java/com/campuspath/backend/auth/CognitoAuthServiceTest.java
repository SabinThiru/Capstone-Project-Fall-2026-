package com.campuspath.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import com.campuspath.backend.common.ApiException;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;


class CognitoAuthServiceTest {

    private final CognitoIdentityProviderClient client = mock(CognitoIdentityProviderClient.class);
    private final CognitoAuthService service = new CognitoAuthService(client, "test-client-id", "test-secret");

    @Test
    void changePasswordSignsInThenChangesWithAccessToken() {
        when(client.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(
                InitiateAuthResponse.builder()
                        .authenticationResult(AuthenticationResultType.builder().accessToken("access-123").build())
                        .build());

        service.changePassword("test@my.unt.edu", "OldPass!1", "NewPass!2");

        ArgumentCaptor<InitiateAuthRequest> login = ArgumentCaptor.forClass(InitiateAuthRequest.class);
        verify(client).initiateAuth(login.capture());
        assertThat(login.getValue().authParameters())
                .containsEntry("USERNAME", "test@my.unt.edu")
                .containsEntry("PASSWORD", "OldPass!1")
                .containsKey("SECRET_HASH");

        ArgumentCaptor<ChangePasswordRequest> change = ArgumentCaptor.forClass(ChangePasswordRequest.class);
        verify(client).changePassword(change.capture());
        assertThat(change.getValue().accessToken()).isEqualTo("access-123");
        assertThat(change.getValue().previousPassword()).isEqualTo("OldPass!1");
        assertThat(change.getValue().proposedPassword()).isEqualTo("NewPass!2");
    }

    @Test
    void wrongCurrentPasswordIs400AndNothingChanges() {
        when(client.initiateAuth(any(InitiateAuthRequest.class)))
                .thenThrow(NotAuthorizedException.builder().message("Incorrect username or password.").build());

        assertThatThrownBy(() -> service.changePassword("test@my.unt.edu", "wrong", "NewPass!2"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("current password is incorrect")
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(client, never()).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void extraLoginChallengeIs400AndNothingChanges() {
        when(client.initiateAuth(any(InitiateAuthRequest.class))).thenReturn(
                InitiateAuthResponse.builder().challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED).build());

        assertThatThrownBy(() -> service.changePassword("test@my.unt.edu", "OldPass!1", "NewPass!2"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(client, never()).changePassword(any(ChangePasswordRequest.class));
    }
}