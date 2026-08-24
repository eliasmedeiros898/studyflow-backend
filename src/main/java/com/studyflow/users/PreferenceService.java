package com.studyflow.users;

import com.studyflow.users.PreferenceModels.PreferencesView;
import com.studyflow.users.PreferenceModels.UpdatePreferencesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PreferenceService {
    private final UserPreferencesRepository preferences;
    private final UserAccountRepository users;

    public PreferenceService(UserPreferencesRepository preferences, UserAccountRepository users) {
        this.preferences = preferences;
        this.users = users;
    }

    @Transactional
    public PreferencesView get(UUID userId) { return view(findOrCreate(userId)); }

    @Transactional
    public PreferencesView update(UUID userId, UpdatePreferencesRequest input) {
        var preference = findOrCreate(userId);
        preference.update(input.focusMinutes(), input.shortBreakMinutes(), input.longBreakMinutes(),
                input.cycles(), input.soundEnabled(), input.browserNotifications(), input.reviewDifficultyDays(),
                input.reviewDevelopingDays(), input.reviewProficientDays(), input.reviewMasteredDays());
        return view(preference);
    }

    private UserPreferences findOrCreate(UUID userId) {
        return preferences.findById(userId).orElseGet(() -> preferences.save(new UserPreferences(
                users.findById(userId).orElseThrow(() -> new NoSuchElementException("Conta não encontrada.")))));
    }

    private PreferencesView view(UserPreferences value) {
        return new PreferencesView(value.getFocusMinutes(), value.getShortBreakMinutes(),
                value.getLongBreakMinutes(), value.getFocusCycles(), value.isSoundEnabled(),
                value.isBrowserNotifications(), value.getReviewDifficultyDays(), value.getReviewDevelopingDays(),
                value.getReviewProficientDays(), value.getReviewMasteredDays());
    }
}
