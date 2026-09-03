package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.common.security.JwtTokenProvider;
import com.kamercinetalents.manager.common.security.SecurityContext;
import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.iam.domain.RoleEntity;
import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import com.kamercinetalents.manager.iam.dto.AuthResponse;
import com.kamercinetalents.manager.iam.dto.Login2FAResponse;
import com.kamercinetalents.manager.iam.dto.LoginRequest;
import com.kamercinetalents.manager.iam.dto.RefreshTokenRequest;
import com.kamercinetalents.manager.iam.repository.PermissionRepository;
import com.kamercinetalents.manager.iam.repository.RoleRepository;
import com.kamercinetalents.manager.iam.repository.UtilisateurRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation de {@link AuthService} — authentification JWT complète.
 *
 * <p>Le flux de login :
 * <ol>
 *   <li>Recherche de l'utilisateur par email</li>
 *   <li>Vérification du mot de passe avec BCrypt</li>
 *   <li>Chargement du rôle et des permissions depuis la base (RBAC dynamique)</li>
 *   <li>Génération du token d'accès (court terme) et du token de rafraîchissement (long terme)</li>
 *   <li>Journalisation de la connexion dans audit_log</li>
 * </ol>
 *
 * <p>Le flux de refresh :
 * <ol>
 *   <li>Validation du token de rafraîchissement</li>
 *   <li>Rechargement de l'utilisateur et de ses permissions</li>
 *   <li>Émission d'un nouveau token d'accès</li>
 * </ol>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    private final TwoFactorService twoFactorService;

    /**
     * Construit le service avec ses dépendances injectées (principe DIP).
     *
     * @param utilisateurRepository repository des utilisateurs
     * @param roleRepository        repository des rôles
     * @param permissionRepository  repository des permissions
     * @param passwordEncoder       encodeur BCrypt
     * @param jwtTokenProvider      fournisseur JWT
     * @param auditService          service d'audit
     * @param twoFactorService      service 2FA
     */
    public AuthServiceImpl(
            UtilisateurRepository utilisateurRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuditService auditService,
            TwoFactorService twoFactorService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
        this.twoFactorService = twoFactorService;
    }

    @Override
    @Transactional
    public Object login(LoginRequest request) {
        UtilisateurEntity user = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

        if (!user.isActif()) {
            throw new IllegalStateException("Compte désactivé — contactez l'administrateur");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }

        if (twoFactorService.is2FAEnabled(user)) {
            twoFactorService.generateCode(user.getId(), user.getEmail());
            return new Login2FAResponse(true, user.getId(), user.getEmail());
        }

        RoleEntity role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Rôle introuvable pour l'utilisateur"));

        List<String> permCodes = permissionRepository.findPermissionCodesByRoleId(user.getRoleId());
        Set<String> permissions = new HashSet<>(permCodes);

        SecurityContext ctx = new SecurityContext(
                user.getId(),
                role.getCode(),
                role.getNiveauHierarchique(),
                user.getTerritoireId(),
                permissions
        );

        String accessToken = jwtTokenProvider.generateAccessToken(ctx);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        auditService.log("login", "utilisateur", user.getId(), null);

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

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new IllegalArgumentException("Token de rafraîchissement invalide ou expiré");
        }

        Claims claims = jwtTokenProvider.getClaims(request.refreshToken());
        String tokenType = claims.get("type", String.class);

        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Token invalide — un token de rafraîchissement est requis");
        }

        UUID userId = UUID.fromString(claims.getSubject());

        UtilisateurEntity user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.isActif()) {
            throw new IllegalStateException("Compte désactivé");
        }

        RoleEntity role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Rôle introuvable"));

        List<String> permCodes = permissionRepository.findPermissionCodesByRoleId(user.getRoleId());
        Set<String> permissions = new HashSet<>(permCodes);

        SecurityContext ctx = new SecurityContext(
                user.getId(),
                role.getCode(),
                role.getNiveauHierarchique(),
                user.getTerritoireId(),
                permissions
        );

        String accessToken = jwtTokenProvider.generateAccessToken(ctx);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

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

    @Override
    @Transactional
    public void logout() {
        UUID userId = SecurityUtils.getCurrentUserId();
        auditService.log("logout", "utilisateur", userId, null);
    }
}
