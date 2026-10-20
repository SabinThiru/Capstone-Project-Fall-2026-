package com.campuspath.backend.user;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.common.ApiException;
import com.campuspath.backend.university.University;
import com.campuspath.backend.university.UniversityRepository;
import com.campuspath.backend.user.UserDtos.*;

@Service
public class UserService {

    private final UserRepository users;
    private final UserSettingsRepository settings;
    private final UniversityRepository universities;

    public UserService(UserRepository users, UserSettingsRepository settings, UniversityRepository universities) {
        this.users = users;
        this.settings = settings;
        this.universities = universities;
    }


    @Transactional
    public UserResponse register(String cognitoSub, String tokenEmail, RegisterRequest req) {
        if (tokenEmail == null || tokenEmail.isBlank()) {
            throw ApiException.badRequest("Token has no email. Send the Cognito ID token, not the access token.");
        }
        if (users.existsByCognitoSub(cognitoSub)) {
            throw ApiException.conflict("This account is already registered");
        }

        University university = universities.findById(req.universityId())
                .orElseThrow(() -> ApiException.badRequest("Unknown university"));


        String email = tokenEmail.trim().toLowerCase(Locale.ROOT);
        String euid = req.euid().trim().toLowerCase(Locale.ROOT);
        String fullName = req.fullName().trim();

        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with this email already exists");
        }
        if (users.existsByUniversity_IdAndEuid(university.getId(), euid)) {
            throw ApiException.conflict("This EUID is already registered at " + university.getName());
        }

        User user = users.saveAndFlush(new User(cognitoSub, university, email, fullName, euid));
        settings.save(new UserSettings(user, university.getTimezone()));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String cognitoSub) {
        return UserResponse.from(findUser(cognitoSub));
    }

    @Transactional(readOnly = true)
    public SettingsResponse getSettings(String cognitoSub) {
        return SettingsResponse.from(findSettings(findUser(cognitoSub)));
    }

    @Transactional
    public SettingsResponse updateSettings(String cognitoSub, UpdateSettingsRequest req) {
        try {
            ZoneId.of(req.timezone());
        } catch (DateTimeException e) {
            throw ApiException.badRequest("Unknown timezone: " + req.timezone());
        }
        UserSettings s = findSettings(findUser(cognitoSub));
        s.setTimezone(req.timezone());
        s.setTheme(req.theme());
        s.setNotificationsEnabled(req.notificationsEnabled());
        s.setEmailRemindersEnabled(req.emailRemindersEnabled());
        s.setDefaultReminderMinutes(req.defaultReminderMinutes());
        return SettingsResponse.from(s);
    }

    private User findUser(String cognitoSub) {

        return users.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> ApiException.notFound("No CampusPath profile for this login yet"));
    }

    private UserSettings findSettings(User user) {
        return settings.findById(user.getId())
                .orElseGet(() -> settings.save(new UserSettings(user, user.getUniversity().getTimezone())));
    }
}