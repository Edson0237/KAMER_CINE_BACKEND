package com.kamercinetalents.manager.sync.dto;

import java.util.List;

public record SyncResponseDto(
        int totalActions,
        int applied,
        int conflicts,
        int rejected,
        List<SyncResultDto> resultats
) {
}
