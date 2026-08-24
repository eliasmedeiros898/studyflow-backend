package com.studyflow.studies;

import com.studyflow.users.UserAccount;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "subjects", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
public class SubjectEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;
    @Column(nullable = false, length = 80) private String name;
    @Column(nullable = false, length = 20) private String color;
    @Column(nullable = false) private boolean archived;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;

    protected SubjectEntity() {}
    public SubjectEntity(UserAccount user, String name, String color) {
        this.id = UUID.randomUUID(); this.user = user; this.name = name; this.color = color;
        this.archived = false; this.createdAt = OffsetDateTime.now();
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public boolean isArchived() { return archived; }
    public void update(String name, String color) { this.name = name; this.color = color; }
    public void archive() { this.archived = true; }
    public void restore() { this.archived = false; }
}
