package com.kamercinetalents.manager.sync.dto;

import java.util.UUID;

public record SyncResultDto(
        UUID id,
        String statut,
        String message
) {
}
