
package com.campuspath.backend.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpResponse;
@Service
public class CognitoAuthService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final String clientId;
    private final String clientSecret;

    public CognitoAuthService(
            @Value("${cognito.user-pool-id}") String userPoolId,
            @Value("${cognito.client-id}") String clientId,
            @Value("${cognito.client-secret}") String clientSecret) {

        String region = userPoolId.substring(0, userPoolId.indexOf('_'));

        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();

        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // Generate the secret hash required by Cognito
    private String calculateSecretHash(String username) {
        try {
            String message = username + clientId;

            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            byte[] hash = mac.doFinal(
                    message.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to calculate Cognito secret hash", e
            );
        }
    }

    // Register a new student in AWS Cognito
    public SignUpResponse register(
            String email,
            String password,
            String fullName) {

        SignUpRequest request = SignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(email))
                .username(email)
                .password(password)
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(email)
                                .build(),
                        AttributeType.builder()
                                .name("name")
                                .value(fullName)
                                .build()
                )
                .build();

        return cognitoClient.signUp(request);
    }

     // Add the email verification method HERE
    public ConfirmSignUpResponse confirmRegistration(
            String email,
            String confirmationCode) {

        ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(email))
                .username(email)
                .confirmationCode(confirmationCode)
                .build();

        return cognitoClient.confirmSignUp(request);
    }


    // Authenticate a student using AWS Cognito
    public InitiateAuthResponse login(String email, String password) {

        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(Map.of(
                        "USERNAME", email,
                        "PASSWORD", password,
                        "SECRET_HASH", calculateSecretHash(email)
                ))
                .build();

        return cognitoClient.initiateAuth(request);
    }


}
