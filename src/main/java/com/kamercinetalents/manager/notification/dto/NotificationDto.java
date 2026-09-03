package com.kamercinetalents.manager.notification.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID templateId,
        String canal,
        String contenuFinal,
        String statut,
        OffsetDateTime dateEnvoi,
        OffsetDateTime dateLecture
) {
}
