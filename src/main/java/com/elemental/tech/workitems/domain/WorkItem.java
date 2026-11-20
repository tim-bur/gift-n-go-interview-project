package com.elemental.tech.workitems.domain;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

@Entity
@Table(name = "work_item")
@Getter
public class WorkItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Priority priority;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WorkItem() {
    }

    public WorkItem(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = Status.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void transitionTo(Status newStatus) {
        if (!this.status.isValidTransition(newStatus)) {
            throw new IllegalStateException("Invalid status transition - " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    //Assuming OPEN requests can be CANCELLED without becoming IN_PROGRESS first
    public enum Status {
        CANCELLED(Set.of()),
        DONE(Set.of()),
        IN_PROGRESS(Set.of(DONE, CANCELLED)),
        OPEN(Set.of(IN_PROGRESS, CANCELLED));

        private final Set<Status> validTransitions;

        Status(Set<Status> validTransitions) {
            this.validTransitions = validTransitions;
        }

        public boolean isValidTransition(Status newStatus) {
            return validTransitions.contains(newStatus);
        }

        public static Status fromString(String string) {
            try {
                return Status.valueOf(string);
            } catch(IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException("Invalid status value: " + string);
            }
        }
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
