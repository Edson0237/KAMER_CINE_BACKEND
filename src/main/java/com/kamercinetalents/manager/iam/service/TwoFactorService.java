package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.iam.domain.RoleEntity;
import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import com.kamercinetalents.manager.iam.dto.AuthResponse;
import com.kamercinetalents.manager.iam.repository.PermissionRepository;
import com.kamercinetalents.manager.iam.repository.RoleRepository;
import com.kamercinetalents.manager.iam.repository.UtilisateurRepository;
import com.kamercinetalents.manager.common.security.JwtTokenProvider;
import com.kamercinetalents.manager.common.security.SecurityContext;
import com.kamercinetalents.manager.common.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service d'authentification à deux facteurs (2FA) par code OTP.
 *
 * <p>Flux :
 * <ol>
 *   <li>Le login standard détecte si l'utilisateur a 2FA activé (metadata.2fa_enabled = true).</li>
 *   <li>Si oui, un code à 6 chiffres est généré et stocké (15 min), le login retourne
 *       {@code twoFactorRequired: true} sans tokens JWT.</li>
 *   <li>Le client appelle {@link #verify} avec le userId et le code.</li>
 *   <li>Si le code est correct, les tokens JWT sont émis.</li>
 * </ol>
 *
 * <p>En production, le code serait envoyé par email ou SMS. En V1, il est loggé.</p>
 */
@Service
public class TwoFactorService {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorService.class);
    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    private final SecureRandom random = new SecureRandom();

    private final Map<UUID, CodeEntry> pendingCodes = new ConcurrentHashMap<>();

    public TwoFactorService(UtilisateurRepository utilisateurRepository,
                            RoleRepository roleRepository,
                            PermissionRepository permissionRepository,
                            JwtTokenProvider jwtTokenProvider,
                            AuditService auditService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    /**
     * Vérifie si un utilisateur a la 2FA activée.
     */
    public boolean is2FAEnabled(UtilisateurEntity user) {
        if (user.getMetadata() == null) return false;
        Object val = user.getMetadata().get("2fa_enabled");
        return Boolean.TRUE.equals(val);
    }

    /**
     * Génère et stocke un code 2FA pour l'utilisateur.
     */
    public void generateCode(UUID userId, String email) {
        String code = generateCode();
        pendingCodes.put(userId, new CodeEntry(code, OffsetDateTime.now()));
        log.info("Code 2FA pour {} : {}", email, code);
    }

    /**
     * Vérifie le code 2FA et émet les tokens JWT si correct.
     *
     * @param userId l'identifiant de l'utilisateur
     * @param code   le code à 6 chiffres
     * @return les tokens JWT et les infos utilisateur
     * @throws IllegalArgumentException si le code est invalide ou expiré
     */
    @Transactional
    public AuthResponse verify(UUID userId, String code) {
        CodeEntry entry = pendingCodes.get(userId);
        if (entry == null || !entry.code().equals(code)) {
            throw new IllegalArgumentException("Code 2FA invalide");
        }

        if (OffsetDateTime.now().isAfter(entry.createdAt().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES))) {
            pendingCodes.remove(userId);
            throw new IllegalArgumentException("Code 2FA expiré");
        }

        UtilisateurEntity user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.isActif()) {
            throw new IllegalStateException("Compte désactivé");
        }

        RoleEntity role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Rôle introuvable"));

        List<String> permCodes = permissionRepository.findPermissionCodesByRoleId(user.getRoleId());
        SecurityContext ctx = new SecurityContext(
                user.getId(),
                role.getCode(),
                role.getNiveauHierarchique(),
                user.getTerritoireId(),
                new HashSet<>(permCodes)
        );

        String accessToken = jwtTokenProvider.generateAccessToken(ctx);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        auditService.log("login_2fa", "utilisateur", user.getId(), null);
        pendingCodes.remove(userId);

        boolean mustChangePassword = user.getMetadata() != null
                && Boolean.TRUE.equals(user.getMetadata().get("must_change_password"));

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId(),
                user.getNom(),
                user.getEmail(),
                role.getCode(),
                role.getNiveauHierarchique(),
                user.getTerritoireId(),
                permCodes,
                mustChangePassword
        );
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private record CodeEntry(String code, OffsetDateTime createdAt) {
    }
}
