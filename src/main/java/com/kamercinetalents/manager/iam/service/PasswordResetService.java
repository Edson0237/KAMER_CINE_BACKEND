package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import com.kamercinetalents.manager.iam.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de réinitialisation de mot de passe par code OTP.
 *
 * <p>Flux :
 * <ol>
 *   <li>{@link #requestReset} — génère un code à 6 chiffres, le stocke en mémoire
 *       avec expiration (15 min), et logge le code (en production, envoi par email/SMS).</li>
 *   <li>{@link #resetPassword} — vérifie le code et met à jour le mot de passe.</li>
 * </ol>
 *
 * <p>En production, le code serait envoyé via email ou SMS. En V1, le code est
 * loggé côté serveur pour permettre les tests sans infrastructure d'envoi.</p>
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    private final Map<String, ResetCodeEntry> resetCodes = new ConcurrentHashMap<>();

    public PasswordResetService(UtilisateurRepository utilisateurRepository,
                                 PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Génère et stocke un code OTP pour l'email donné.
     *
     * @param email l'email de l'utilisateur
     */
    @Transactional
    public void requestReset(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(user -> {
            String code = generateCode();
            resetCodes.put(email, new ResetCodeEntry(code, OffsetDateTime.now()));
            log.info("Code de réinitialisation pour {} : {}", email, code);
        });
    }

    /**
     * Vérifie le code et réinitialise le mot de passe.
     *
     * @param email       l'email de l'utilisateur
     * @param code        le code OTP reçu
     * @param newPassword le nouveau mot de passe
     * @throws IllegalArgumentException si le code est invalide ou expiré
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        ResetCodeEntry entry = resetCodes.get(email);
        if (entry == null || !entry.code().equals(code)) {
            throw new IllegalArgumentException("Code de réinitialisation invalide");
        }

        if (OffsetDateTime.now().isAfter(entry.createdAt().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))) {
            resetCodes.remove(email);
            throw new IllegalArgumentException("Code de réinitialisation expiré");
        }

        UtilisateurEntity user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setServerUpdatedAt(OffsetDateTime.now());
        utilisateurRepository.save(user);

        resetCodes.remove(email);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private record ResetCodeEntry(String code, OffsetDateTime createdAt) {
    }
}
