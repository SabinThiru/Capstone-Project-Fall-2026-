

package com.campuspath.backend.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CognitoAuthService cognitoAuthService;

    public AuthController(CognitoAuthService cognitoAuthService) {
        this.cognitoAuthService = cognitoAuthService;
    }

    // 1. Registration request
    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String fullName
    ) {}

    // 2. Email confirmation request
    public record ConfirmRequest(
            @NotBlank @Email String email,
            @NotBlank String confirmationCode
    ) {}

    // 3. Login request
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    // Registration response
    public record RegisterResponse(
            String message,
            boolean confirmationRequired
    ) {}

    // Login response
    public record LoginResponse(
            String accessToken,
            String idToken,
            int expiresIn
    ) {}

    // Register a new student
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        SignUpResponse result = cognitoAuthService.register(
                request.email(),
                request.password(),
                request.fullName()
        );

        return ResponseEntity.ok(
                new RegisterResponse(
                        "Registration submitted. Check your email for a verification code.",
                        !Boolean.TRUE.equals(result.userConfirmed())
                )
        );
    }

    // Confirm student's email verification code
    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(
            @Valid @RequestBody ConfirmRequest request) {

        cognitoAuthService.confirmRegistration(
                request.email(),
                request.confirmationCode()
        );

        return ResponseEntity.ok(
                "Email verified successfully. You can now log in."
        );
    }

    // Authenticate a student using AWS Cognito
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        InitiateAuthResponse result = cognitoAuthService.login(
                request.email(),
                request.password()
        );

        if (result.authenticationResult() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Additional authentication is required"
            );
        }

        var tokens = result.authenticationResult();

        return ResponseEntity.ok(
                new LoginResponse(
                        tokens.accessToken(),
                        tokens.idToken(),
                        tokens.expiresIn()
                )
        );
    }
}
