package com.kamercinetalents.manager.notification.service;

import com.kamercinetalents.manager.admin.repository.ParametreSystemeRepository;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.notification.domain.NotificationEntity;
import com.kamercinetalents.manager.notification.domain.SmsLogEntity;
import com.kamercinetalents.manager.notification.domain.TemplateNotificationEntity;
import com.kamercinetalents.manager.notification.dto.*;
import com.kamercinetalents.manager.notification.repository.NotificationRepository;
import com.kamercinetalents.manager.notification.repository.SmsLogRepository;
import com.kamercinetalents.manager.notification.repository.TemplateNotificationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service de notifications — module M6 (socle léger V1).
 *
 * <p>Mécanisme de bascule SMS pour les zones sans data :</p>
 * <ol>
 *   <li>Tente d'envoyer la notification via le canal {@code in_app} (data).</li>
 *   <li>Si l'utilisateur n'a pas de connexion data (détecté via l'absence de
 *       lecture après un délai, ou forcé via {@code forcerSms=true}), bascule
 *       vers le canal SMS.</li>
 *   <li>Chaque envoi SMS est tracé dans {@code sms_log} (coût, fournisseur, statut).</li>
 * </ol>
 *
 * <p>Le seuil de bascule (délai avant fallback SMS) est configurable via
 * le paramètre système {@code notification.delai_bascule_sms_secondes}.</p>
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TemplateNotificationRepository templateRepository;
    private final SmsLogRepository smsLogRepository;
    private final ParametreSystemeRepository parametreRepository;
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            TemplateNotificationRepository templateRepository,
            SmsLogRepository smsLogRepository,
            ParametreSystemeRepository parametreRepository,
            JdbcTemplate jdbcTemplate) {
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
        this.smsLogRepository = smsLogRepository;
        this.parametreRepository = parametreRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Envoie une notification à un utilisateur.
     *
     * <p>Si {@code forcerSms=true} ou si le paramètre système indique que
     * l'utilisateur est en zone sans data, envoie directement via SMS.
     * Sinon, tente d'abord {@code in_app} et programme une bascule SMS
     * si non lu après le délai configuré.</p>
     *
     * @param request la requête avec code template, utilisateur, variables
     * @return la notification créée
     */
    public NotificationDto envoyer(SendNotificationRequest request) {
        if (!SecurityUtils.get().permissions().contains("notification:write")) {
            throw new com.kamercinetalents.manager.common.exception.PerimeterAccessException(
                    "Permission requise: notification:write");
        }
        String canal = request.forcerSms() ? "sms" : "in_app";
        String templateCode = request.forcerSms()
                ? deriveSmsCode(request.templateCode())
                : request.templateCode();

        TemplateNotificationEntity template = templateRepository
                .findByCodeAndCanalAndActifTrue(templateCode, canal)
                .or(() -> templateRepository.findByCode(templateCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Template introuvable: " + templateCode));

        String contenuFinal = renderTemplate(template.getCorps(), request.variables());

        NotificationEntity notif = new NotificationEntity();
        notif.setId(UUID.randomUUID());
        notif.setTemplateId(template.getId());
        notif.setUtilisateurId(request.utilisateurId());
        notif.setCanal(canal);
        notif.setContenuFinal(contenuFinal);
        notif.setStatut("en_attente");
        notificationRepository.save(notif);

        if ("sms".equals(canal)) {
            sendSms(notif, request.utilisateurId());
        }

        return toDto(notif);
    }

    /**
     * Bascule une notification in_app vers SMS si elle n'a pas été lue
     * après le délai configuré. Appelé par un job planifié.
     */
    public int basculerVersSms() {
        int delaiSecondes = parametreRepository
                .findByCle("notification.delai_bascule_sms_secondes")
                .map(p -> Integer.parseInt(p.getValeur()))
                .orElse(300);

        OffsetDateTime seuil = OffsetDateTime.now().minusSeconds(delaiSecondes);

        List<NotificationEntity> pending = notificationRepository.findAll().stream()
                .filter(n -> "in_app".equals(n.getCanal())
                        && "en_attente".equals(n.getStatut())
                        && n.getDateEnvoi() != null
                        && n.getDateEnvoi().isBefore(seuil))
                .toList();

        int count = 0;
        for (NotificationEntity notif : pending) {
            TemplateNotificationEntity template = templateRepository
                    .findById(notif.getTemplateId()).orElse(null);
            if (template == null) continue;

            String smsCode = deriveSmsCode(template.getCode());
            TemplateNotificationEntity smsTemplate = templateRepository
                    .findByCodeAndCanalAndActifTrue(smsCode, "sms").orElse(null);

            if (smsTemplate != null) {
                notif.setCanal("sms");
                notif.setContenuFinal(smsTemplate.getCorps());
                notificationRepository.save(notif);
                sendSms(notif, notif.getUtilisateurId());
                count++;
            }
        }
        return count;
    }

    /**
     * Marque une notification comme lue.
     */
    public void marquerCommeLue(UUID notificationId) {
        NotificationEntity notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable: " + notificationId));
        UUID currentUser = SecurityUtils.getCurrentUserId();
        if (!notif.getUtilisateurId().equals(currentUser)) {
            throw new IllegalStateException("Notification non accessible");
        }
        notif.setStatut("lue");
        notif.setDateLecture(OffsetDateTime.now());
        notificationRepository.save(notif);
    }

    /**
     * Liste les notifications de l'utilisateur courant.
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> getMesNotifications() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Compte les notifications non lues de l'utilisateur courant.
     */
    @Transactional(readOnly = true)
    public long countNonLues() {
        UUID userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByUtilisateurIdAndStatut(userId, "en_attente");
    }

    // ==================== PRIVÉ ====================

    /**
     * Envoie une notification système (sans vérification de permission).
     *
     * <p>Utilisé pour les notifications générées par le système (ex: envoi
     * des identifiants de connexion après acceptation d'une candidature).
     * Le canal est déterminé par l'appelant (sms ou in_app).</p>
     *
     * @param utilisateurId l'identifiant de l'utilisateur destinataire
     * @param contenu       le contenu de la notification
     * @param forcerSms     true pour forcer l'envoi SMS
     */
    public void envoyerNotificationSysteme(UUID utilisateurId, String contenu, boolean forcerSms) {
        String canal = forcerSms ? "sms" : "in_app";

        NotificationEntity notif = new NotificationEntity();
        notif.setId(UUID.randomUUID());
        notif.setTemplateId(null);
        notif.setUtilisateurId(utilisateurId);
        notif.setCanal(canal);
        notif.setContenuFinal(contenu);
        notif.setStatut("en_attente");
        notificationRepository.save(notif);

        if ("sms".equals(canal)) {
            sendSms(notif, utilisateurId);
        }
    }

    /**
     * Envoie un SMS via le fournisseur configuré et trace dans sms_log.
     * En V1, l'envoi réel est simulé (log only). L'intégration Twilio/Orange/MTN
     * se fera en V2 via un SmsProviderService.
     */
    private void sendSms(NotificationEntity notif, UUID utilisateurId) {
        String numero = getNumeroUtilisateur(utilisateurId);
        if (numero == null || numero.isBlank()) {
            notif.setStatut("echec");
            notificationRepository.save(notif);
            return;
        }

        String fournisseur = parametreRepository
                .findByCle("notification.fournisseur_sms_defaut")
                .map(p -> p.getValeur())
                .orElse("orange");

        SmsLogEntity log = new SmsLogEntity();
        log.setId(UUID.randomUUID());
        log.setNotificationId(notif.getId());
        log.setNumeroDestinataire(numero);
        log.setFournisseur(fournisseur);
        log.setStatutFournisseur("simule_v1");
        log.setCout(new BigDecimal("15"));
        log.setDateEnvoi(OffsetDateTime.now());
        log.setTentative((short) 1);
        smsLogRepository.save(log);

        notif.setStatut("envoyee");
        notif.setDateEnvoi(OffsetDateTime.now());
        notificationRepository.save(notif);
    }

    /**
     * Récupère le numéro de téléphone de l'utilisateur depuis la base.
     */
    private String getNumeroUtilisateur(UUID utilisateurId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT telephone FROM utilisateur WHERE id = ?",
                    String.class, utilisateurId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Dérive le code template SMS à partir du code in_app.
     * Ex: "session.cloturee" → "session.cloturee_sms"
     */
    private String deriveSmsCode(String code) {
        if (code.endsWith("_sms")) return code;
        return code + "_sms";
    }

    /**
     * Remplace les placeholders {{variable}} par les valeurs fournies.
     */
    private String renderTemplate(String corps, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) return corps;
        String result = corps;
        for (var entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private NotificationDto toDto(NotificationEntity e) {
        return new NotificationDto(
                e.getId(), e.getTemplateId(), e.getCanal(),
                e.getContenuFinal(), e.getStatut(),
                e.getDateEnvoi(), e.getDateLecture());
    }
}
