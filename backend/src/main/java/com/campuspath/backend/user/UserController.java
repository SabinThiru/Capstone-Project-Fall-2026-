package com.campuspath.backend.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.campuspath.backend.user.UserDtos.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegisterRequest body) {
        return userService.register(jwt.getSubject(), jwt.getClaimAsString("email"), body);
    }


    @GetMapping
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(jwt.getSubject());
    }

    @GetMapping("/settings")
    public SettingsResponse getSettings(@AuthenticationPrincipal Jwt jwt) {
        return userService.getSettings(jwt.getSubject());
    }

    @PutMapping("/settings")
    public SettingsResponse updateSettings(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody UpdateSettingsRequest body) {
        return userService.updateSettings(jwt.getSubject(), body);
    }
}