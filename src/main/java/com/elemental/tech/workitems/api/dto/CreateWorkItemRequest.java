package com.elemental.tech.workitems.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorkItemRequest(
        //Assuming 30/300 size limits are sufficient
        @NotBlank(message = "Title must not be blank")
        @Size(max = 30, message = "Title must not be longer than 30 characters")
        String title,
        @NotBlank(message = "Description must not be blank")
        @Size(max = 300, message = "Description must not be longer than 300 characters")
        String description,
        @NotNull(message = "Priority must be provided")
        Priority priority
) {

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}
