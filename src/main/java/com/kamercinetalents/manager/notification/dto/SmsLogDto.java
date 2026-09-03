package com.kamercinetalents.manager.notification.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SmsLogDto(
        UUID id,
        UUID notificationId,
        String numeroDestinataire,
        String fournisseur,
        String statutFournisseur,
        BigDecimal cout,
        OffsetDateTime dateEnvoi,
        short tentative
) {
}
