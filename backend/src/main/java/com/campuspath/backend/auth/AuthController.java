package com.campuspath.backend.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    // Only accepts university email addresses ending in .edu
    public record RegisterRequest(
            @NotBlank
            @Email
            @Pattern(
                    regexp = "(?i)^[^\\s@]+@[^\\s@]+\\.edu$",
                    message = "Please register using a university email ending in .edu"
            )
            String email,

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

    // 4. Registration response
    public record RegisterResponse(
            String message,
            boolean confirmationRequired
    ) {}

    // 5. Login response
    public record LoginResponse(
            String accessToken,
            String idToken,
            int expiresIn
    ) {}

    // 6. Register a new student
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

    // 7. Confirm student's email verification code
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

    // 8. Authenticate a student using AWS Cognito
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

    // 9. Forgot Password request
    public record ForgotPasswordRequest(
            @NotBlank @Email String email
    ) {}

    // 10. Reset Password request
    public record ResetPasswordRequest(
            @NotBlank @Email String email,
            @NotBlank String confirmationCode,
            @NotBlank @Size(min = 8) String newPassword
    ) {}

    // 11. Send password reset verification code using AWS Cognito
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        // Keep responses generic so callers cannot identify
        // whether an email address is registered.
        try {
            cognitoAuthService.forgotPassword(request.email());

        } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException ignored) {
            // Do not disclose whether an account exists.
        }

        return ResponseEntity.ok(
                "If the account is eligible, a password reset code will be sent."
        );
    }

    // 12. Reset password using verification code
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        cognitoAuthService.resetPassword(
                request.email(),
                request.confirmationCode(),
                request.newPassword()
        );

        return ResponseEntity.ok(
                "Password reset successfully. You can now log in."
        );
    }
}