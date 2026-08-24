package com.studyflow.users;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_accounts")
public class UserAccount {
    @Id
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 80)
    private String timezone;
    @Column(name = "target_exam_name", length = 120)
    private String targetExamName;
    @Column(name = "target_exam_date")
    private LocalDate targetExamDate;
    @Column(name = "token_version", nullable = false)
    private int tokenVersion;
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;
    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserAccount() {}

    public UserAccount(String name, String email, String passwordHash, String timezone) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.timezone = timezone;
        this.tokenVersion = 0;
        this.failedLoginAttempts = 0;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getTimezone() { return timezone; }
    public String getTargetExamName() { return targetExamName; }
    public LocalDate getTargetExamDate() { return targetExamDate; }
    public int getTokenVersion() { return tokenVersion; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public OffsetDateTime getLockedUntil() { return lockedUntil; }

    public void updateProfile(String name, String timezone, String targetExamName, LocalDate targetExamDate) {
        this.name = name;
        this.timezone = timezone;
        this.targetExamName = targetExamName;
        this.targetExamDate = targetExamDate;
        this.updatedAt = OffsetDateTime.now();
    }

    public void registerFailedLogin(int maximumAttempts, OffsetDateTime lockUntil) {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= maximumAttempts) this.lockedUntil = lockUntil;
        this.updatedAt = OffsetDateTime.now();
    }

    public void clearFailedLogins() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.tokenVersion++;
        clearFailedLogins();
    }

    public void changeEmail(String email) {
        this.email = email;
        this.updatedAt = OffsetDateTime.now();
    }
}
