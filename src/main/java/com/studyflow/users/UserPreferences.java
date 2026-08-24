package com.studyflow.users;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id
    @Column(name = "user_id")
    private java.util.UUID userId;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserAccount user;
    @Column(name = "focus_minutes", nullable = false)
    private int focusMinutes;
    @Column(name = "short_break_minutes", nullable = false)
    private int shortBreakMinutes;
    @Column(name = "long_break_minutes", nullable = false)
    private int longBreakMinutes;
    @Column(name = "focus_cycles", nullable = false)
    private int focusCycles;
    @Column(name = "sound_enabled", nullable = false)
    private boolean soundEnabled;
    @Column(name = "browser_notifications", nullable = false)
    private boolean browserNotifications;
    @Column(name = "review_difficulty_days", nullable = false)
    private int reviewDifficultyDays;
    @Column(name = "review_developing_days", nullable = false)
    private int reviewDevelopingDays;
    @Column(name = "review_proficient_days", nullable = false)
    private int reviewProficientDays;
    @Column(name = "review_mastered_days", nullable = false)
    private int reviewMasteredDays;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserPreferences() {}

    public UserPreferences(UserAccount user) {
        this.user = user;
        this.focusMinutes = 25;
        this.shortBreakMinutes = 5;
        this.longBreakMinutes = 15;
        this.focusCycles = 4;
        this.soundEnabled = true;
        this.browserNotifications = false;
        this.reviewDifficultyDays = 1;
        this.reviewDevelopingDays = 3;
        this.reviewProficientDays = 7;
        this.reviewMasteredDays = 15;
        this.updatedAt = OffsetDateTime.now();
    }

    public int getFocusMinutes() { return focusMinutes; }
    public int getShortBreakMinutes() { return shortBreakMinutes; }
    public int getLongBreakMinutes() { return longBreakMinutes; }
    public int getFocusCycles() { return focusCycles; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isBrowserNotifications() { return browserNotifications; }
    public int getReviewDifficultyDays() { return reviewDifficultyDays; }
    public int getReviewDevelopingDays() { return reviewDevelopingDays; }
    public int getReviewProficientDays() { return reviewProficientDays; }
    public int getReviewMasteredDays() { return reviewMasteredDays; }

    public void update(int focusMinutes, int shortBreakMinutes, int longBreakMinutes, int focusCycles,
                       boolean soundEnabled, boolean browserNotifications, int reviewDifficultyDays,
                       int reviewDevelopingDays, int reviewProficientDays, int reviewMasteredDays) {
        this.focusMinutes = focusMinutes;
        this.shortBreakMinutes = shortBreakMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.focusCycles = focusCycles;
        this.soundEnabled = soundEnabled;
        this.browserNotifications = browserNotifications;
        this.reviewDifficultyDays = reviewDifficultyDays;
        this.reviewDevelopingDays = reviewDevelopingDays;
        this.reviewProficientDays = reviewProficientDays;
        this.reviewMasteredDays = reviewMasteredDays;
        this.updatedAt = OffsetDateTime.now();
    }
}
