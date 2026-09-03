package com.kamercinetalents.manager.sync.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SyncRequestDto(
        @NotNull UUID utilisateurId,
        @Size(max = 100) String deviceId,
        @NotEmpty @Size(max = 500) List<SyncActionDto> actions
) {
}
