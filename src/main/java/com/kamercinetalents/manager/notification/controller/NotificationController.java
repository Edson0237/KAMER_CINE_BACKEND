package com.kamercinetalents.manager.notification.controller;

import com.kamercinetalents.manager.notification.dto.NotificationDto;
import com.kamercinetalents.manager.notification.dto.SendNotificationRequest;
import com.kamercinetalents.manager.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "M6 — Notifications", description = "Notifications in_app et SMS avec bascule pour zones sans data")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Lister mes notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des notifications",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class)))
    })
    public ResponseEntity<List<NotificationDto>> getMesNotifications() {
        return ResponseEntity.ok(notificationService.getMesNotifications());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Compter mes notifications non lues")
    public ResponseEntity<Map<String, Long>> countNonLues() {
        return ResponseEntity.ok(Map.of("nonLues", notificationService.countNonLues()));
    }

    @PostMapping
    @Operation(
            summary = "Envoyer une notification",
            description = """
                    Envoie une notification à un utilisateur.
                    - Canal in_app par défaut (data)
                    - Si forcerSms=true, envoie directement via SMS
                    - Bascule automatique in_app → SMS si non lu après délai configurable
                      (paramètre notification.delai_bascule_sms_secondes, défaut 300s)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification envoyée",
                    content = @Content(schema = @Schema(implementation = NotificationDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "a1b2c3d4-...",
                                      "templateId": "e5f6g7h8-...",
                                      "canal": "in_app",
                                      "contenuFinal": "La session de formation Yaoundé a été clôturée...",
                                      "statut": "en_attente",
                                      "dateEnvoi": null,
                                      "dateLecture": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Template introuvable"),
            @ApiResponse(responseCode = "401", description = "JWT manquant ou invalide")
    })
    public ResponseEntity<NotificationDto> envoyer(@Valid @RequestBody SendNotificationRequest request) {
        return ResponseEntity.ok(notificationService.envoyer(request));
    }

    @PostMapping("/{id}/lue")
    @Operation(summary = "Marquer une notification comme lue")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marquée comme lue"),
            @ApiResponse(responseCode = "403", description = "Notification non accessible"),
            @ApiResponse(responseCode = "404", description = "Notification introuvable")
    })
    public ResponseEntity<Void> marquerCommeLue(@PathVariable UUID id) {
        notificationService.marquerCommeLue(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bascule-sms")
    @Operation(
            summary = "Déclencher la bascule SMS",
            description = """
                    Bascule les notifications in_app non lues après le délai configuré
                    vers le canal SMS. Retourne le nombre de notifications basculées.
                    À appeler par un cron job (ex: toutes les 5 minutes).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bascule exécutée",
                    content = @Content(examples = @ExampleObject(value = "{\"basculées\": 3}")))
    })
    public ResponseEntity<Map<String, Integer>> basculeSms() {
        int count = notificationService.basculerVersSms();
        return ResponseEntity.ok(Map.of("basculées", count));
    }
}
