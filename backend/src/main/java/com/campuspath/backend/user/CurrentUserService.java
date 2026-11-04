package com.campuspath.backend.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.common.ApiException;

@Service
public class CurrentUserService {

    private final UserRepository users;
    private final UserSettingsRepository settings;

    public CurrentUserService(UserRepository users, UserSettingsRepository settings) {
        this.users = users;
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public User require(String cognitoSub) {
        return users.findByCognitoSub(cognitoSub)
                .orElseThrow(() -> ApiException.notFound("No CampusPath profile for this login yet"));
    }

    @Transactional(readOnly = true)
    public Integer requireId(String cognitoSub) {
        return require(cognitoSub).getId();
    }

    @Transactional(readOnly = true)
    public String timezoneOf(User user) {
        return settings.findById(user.getId())
                .map(UserSettings::getTimezone)
                .orElse(user.getUniversity().getTimezone());
    }
}