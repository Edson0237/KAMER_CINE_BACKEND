package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.common.security.SecurityUtils;
import com.kamercinetalents.manager.common.service.AuditService;
import com.kamercinetalents.manager.common.service.TerritoireAccessService;
import com.kamercinetalents.manager.iam.domain.RoleEntity;
import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import com.kamercinetalents.manager.iam.dto.ChangePasswordRequest;
import com.kamercinetalents.manager.iam.dto.CreateUtilisateurRequest;
import com.kamercinetalents.manager.iam.dto.UtilisateurDto;
import com.kamercinetalents.manager.iam.repository.RoleRepository;
import com.kamercinetalents.manager.iam.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implémentation du service de gestion des utilisateurs.
 *
 * <p>Respecte le principe SRP : ne contient que la logique métier liée
 * aux utilisateurs. Le hashage du mot de passe est délégué à
 * {@link PasswordEncoder} injecté. Le filtrage territorial est appliqué
 * via {@link TerritoireAccessService} — chaque utilisateur ne voit que
 * les utilisateurs de son périmètre. Toute création est journalisée
 * dans audit_log.</p>
 */
@Service
@Transactional
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final TerritoireAccessService territoireAccessService;

    /**
     * Construit le service avec ses dépendances injectées (principe DIP).
     *
     * @param repository              le repository JPA des utilisateurs
     * @param roleRepository          le repository JPA des rôles (résolution du roleCode)
     * @param passwordEncoder         l'encodeur de mots de passe BCrypt
     * @param auditService            le service d'audit transverse
     * @param territoireAccessService le service de contrôle du périmètre territorial
     */
    public UtilisateurServiceImpl(
            UtilisateurRepository repository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            TerritoireAccessService territoireAccessService) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.territoireAccessService = territoireAccessService;
    }

    @Override
    public UtilisateurDto create(CreateUtilisateurRequest request) {
        // Vérifier que le territoire assigné est dans le périmètre de l'utilisateur courant
        if (request.territoireId() != null) {
            territoireAccessService.requireAccess(request.territoireId());
        }

        UtilisateurEntity entity = new UtilisateurEntity(
                UUID.randomUUID(),
                request.nom(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.roleId(),
                request.territoireId()
        );
        entity.setTelephone(request.telephone());
        entity.setSyncStatus("synced");
        entity.setServerUpdatedAt(OffsetDateTime.now());

        UtilisateurEntity saved = repository.save(entity);

        auditService.log("create", "utilisateur", saved.getId(),
                Map.of("email", saved.getEmail(), "role_id", saved.getRoleId().toString()));

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto getById(UUID id) {
        UtilisateurEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable: " + id));

        // Vérifier le périmètre territorial de l'utilisateur consulté
        if (entity.getTerritoireId() != null) {
            territoireAccessService.requireAccess(entity.getTerritoireId());
        }

        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurDto> listForCurrentTerritoire() {
        UUID perimeter = territoireAccessService.getCurrentPerimeter();
        if (perimeter == null) {
            // N1 sans territoire spécifique — retourne tous les utilisateurs
            return repository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        }
        // Filtrer : ne retourner que les utilisateurs dont le territoire
        // est dans le périmètre de l'utilisateur courant
        return repository.findAll().stream()
                .filter(u -> u.getTerritoireId() == null || territoireAccessService.canAccess(u.getTerritoireId()))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurDto getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId();
        UtilisateurEntity entity = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable: " + userId));
        return toDto(entity);
    }

    @Override
    public void changeOwnPassword(ChangePasswordRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UtilisateurEntity entity = repository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur courant introuvable: " + userId));

        if (!passwordEncoder.matches(request.currentPassword(), entity.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        entity.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        Map<String, Object> metadata = entity.getMetadata() != null
                ? new HashMap<>(entity.getMetadata())
                : new HashMap<>();
        metadata.remove("must_change_password");
        entity.setMetadata(metadata);
        entity.setServerUpdatedAt(OffsetDateTime.now());

        repository.save(entity);
        auditService.log("change_password", "utilisateur", userId, Map.of());
    }

    /**
     * Convertit une entité JPA en DTO immuable.
     *
     * <p>Cette conversion garantit qu'aucune colonne technique (hash,
     * sync_status) n'est exposée à l'API.</p>
     *
     * @param entity l'entité à convertir
     * @return le DTO correspondant
     */
    private UtilisateurDto toDto(UtilisateurEntity entity) {
        String roleCode = roleRepository.findById(entity.getRoleId())
                .map(RoleEntity::getCode)
                .orElse(null);
        boolean mustChangePassword = entity.getMetadata() != null
                && Boolean.TRUE.equals(entity.getMetadata().get("must_change_password"));
        return new UtilisateurDto(
                entity.getId(),
                entity.getNom(),
                entity.getEmail(),
                entity.getTelephone(),
                entity.isActif(),
                entity.getRoleId(),
                entity.getTerritoireId(),
                roleCode,
                mustChangePassword
        );
    }
}
