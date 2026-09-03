package com.kamercinetalents.manager.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;
import java.util.UUID;

public record SendNotificationRequest(
        @NotBlank @Size(max = 100) String templateCode,
        @NotNull UUID utilisateurId,
        Map<String, String> variables,
        boolean forcerSms
) {
}
