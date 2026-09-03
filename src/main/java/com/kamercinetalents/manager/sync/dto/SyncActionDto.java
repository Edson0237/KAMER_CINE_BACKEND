package com.kamercinetalents.manager.sync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record SyncActionDto(
        @NotNull UUID id,
        @NotBlank @Size(max = 50) String entiteType,
        @NotNull UUID entiteId,
        @NotBlank @Size(max = 20) String operation,
        Map<String, Object> payload,
        @NotNull OffsetDateTime horodatageClient
) {
}
