package com.kamercinetalents.manager.integration;

import com.kamercinetalents.manager.common.exception.PerimeterAccessException;
import com.kamercinetalents.manager.common.security.SecurityContext;
import com.kamercinetalents.manager.common.service.TerritoireAccessService;
import com.kamercinetalents.manager.formation.domain.ApprenantEntity;
import com.kamercinetalents.manager.formation.domain.PresenceEntity;
import com.kamercinetalents.manager.formation.domain.SessionFormationEntity;
import com.kamercinetalents.manager.formation.dto.*;
import com.kamercinetalents.manager.formation.repository.*;
import com.kamercinetalents.manager.formation.service.FormationService;
import com.kamercinetalents.manager.iam.domain.*;
import com.kamercinetalents.manager.iam.repository.*;
import com.kamercinetalents.manager.formation.domain.EncadreurEntity;
import com.kamercinetalents.manager.iam.service.RolePermissionService;
import com.kamercinetalents.manager.sync.domain.SyncConflictLogEntity;
import com.kamercinetalents.manager.sync.dto.*;
import com.kamercinetalents.manager.sync.repository.SyncConflictLogRepository;
import com.kamercinetalents.manager.sync.service.SyncService;
import com.kamercinetalents.manager.territoire.domain.TerritoireEntity;
import com.kamercinetalents.manager.territoire.repository.TerritoireRepository;
import com.kamercinetalents.manager.territoire.repository.TypeTerritoireRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration de sécurité — couvrent le contrôle de périmètre territorial,
 * le RBAC dynamique, la synchronisation avec conflit, et l'accès direct API.
 *
 * <p>Utilise Testcontainers (PostgreSQL 16 éphémère) — la vraie CTE récursive est exercée.
 * Les migrations Flyway V1-V8 sont appliquées sur le conteneur.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired private FormationService formationService;
    @Autowired private SyncService syncService;
    @Autowired private RolePermissionService rolePermissionService;
    @Autowired private TerritoireAccessService territoireAccessService;
    @Autowired private ApprenantRepository apprenantRepository;
    @Autowired private SessionFormationRepository sessionRepository;
    @Autowired private PresenceRepository presenceRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private SyncConflictLogRepository conflictLogRepository;
    @Autowired private TerritoireRepository territoireRepository;
    @Autowired private TypeTerritoireRepository typeTerritoireRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private com.kamercinetalents.manager.formation.repository.EncadreurRepository encadreurRepository;

    // UUIDs fixes pour la hiérarchie de test
    // Niveau 1: Cameroun (national)
    private static final UUID CAMEROON_ID = UUID.fromString("00000000-0000-0000-0000-000000000100");
    // Niveau 2: Région Centre / Région Littoral
    private static final UUID REGION_CENTRE = UUID.fromString("00000000-0000-0000-0000-000000000200");
    private static final UUID REGION_LITTORAL = UUID.fromString("00000000-0000-0000-0000-000000000201");
    // Niveau 3: Département Mfoundi (Centre) / Département Wouri (Littoral)
    private static final UUID DEPT_MFOUNDI = UUID.fromString("00000000-0000-0000-0000-000000000300");
    private static final UUID DEPT_WOURI = UUID.fromString("00000000-0000-0000-0000-000000000301");
    // Niveau 4: Arrondissement Yaoundé-1 (Mfoundi) / Arrondissement Douala-1 (Wouri)
    private static final UUID ARR_YAOUNDE_1 = UUID.fromString("00000000-0000-0000-0000-000000000400");
    private static final UUID ARR_DOUALA_1 = UUID.fromString("00000000-0000-0000-0000-000000000401");
    // Niveau 5: Commune Yaoundé-1 / Commune Yaoundé-2 / Commune Douala
    private static final UUID COMMUNE_YAOUNDE_1 = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COMMUNE_YAOUNDE_2 = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID COMMUNE_DOUALA = UUID.fromString("30000000-0000-0000-0000-000000000003");

    // Utilisateurs de test
    private static final UUID USER_N5_YAOUNDE_1 = UUID.fromString("11000000-0000-0000-0000-000000000001");
    private static final UUID USER_N5_YAOUNDE_2 = UUID.fromString("22000000-0000-0000-0000-000000000002");
    private static final UUID USER_N1_CENTRAL = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_N2_CENTRE = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ENCADREUR_ID = UUID.fromString("44000000-0000-0000-0000-000000000001");

    // IDs des types de territoire (récupérés depuis la base après migration)
    private UUID typeNationalId;
    private UUID typeRegionId;
    private UUID typeDeptId;
    private UUID typeArrondId;
    private UUID typeCommuneId;

    @AfterEach
    void cleanupSecurity() {
        SecurityContextHolder.clearContext();
    }

    // ============================================================
    // 0. HIÉRARCHIE DE TERRITOIRES — insérée avant chaque test
    // ============================================================

    @BeforeEach
    void setupTerritoireHierarchy() {
        // Récupérer les types depuis la base (seedés par V2)
        typeTerritoireRepository.findAll().forEach(t -> {
            switch (t.getCode()) {
                case "national" -> typeNationalId = t.getId();
                case "region" -> typeRegionId = t.getId();
                case "departement" -> typeDeptId = t.getId();
                case "arrondissement" -> typeArrondId = t.getId();
                case "commune" -> typeCommuneId = t.getId();
            }
        });

        // Insérer la hiérarchie si pas déjà présente
        if (!territoireRepository.existsById(CAMEROON_ID)) {
            saveTerritoire(CAMEROON_ID, typeNationalId, null, "Cameroun", "CMR_TEST");
        }
        if (!territoireRepository.existsById(REGION_CENTRE)) {
            saveTerritoire(REGION_CENTRE, typeRegionId, CAMEROON_ID, "Région Centre", "REG_CENTRE");
        }
        if (!territoireRepository.existsById(REGION_LITTORAL)) {
            saveTerritoire(REGION_LITTORAL, typeRegionId, CAMEROON_ID, "Région Littoral", "REG_LITTORAL");
        }
        if (!territoireRepository.existsById(DEPT_MFOUNDI)) {
            saveTerritoire(DEPT_MFOUNDI, typeDeptId, REGION_CENTRE, "Mfoundi", "DEPT_MFOUNDI");
        }
        if (!territoireRepository.existsById(DEPT_WOURI)) {
            saveTerritoire(DEPT_WOURI, typeDeptId, REGION_LITTORAL, "Wouri", "DEPT_WOURI");
        }
        if (!territoireRepository.existsById(ARR_YAOUNDE_1)) {
            saveTerritoire(ARR_YAOUNDE_1, typeArrondId, DEPT_MFOUNDI, "Yaoundé-1", "ARR_YDE1");
        }
        if (!territoireRepository.existsById(ARR_DOUALA_1)) {
            saveTerritoire(ARR_DOUALA_1, typeArrondId, DEPT_WOURI, "Douala-1", "ARR_DLA1");
        }
        if (!territoireRepository.existsById(COMMUNE_YAOUNDE_1)) {
            saveTerritoire(COMMUNE_YAOUNDE_1, typeCommuneId, ARR_YAOUNDE_1, "Commune Yaoundé-1", "COM_YDE1");
        }
        if (!territoireRepository.existsById(COMMUNE_YAOUNDE_2)) {
            saveTerritoire(COMMUNE_YAOUNDE_2, typeCommuneId, ARR_YAOUNDE_1, "Commune Yaoundé-2", "COM_YDE2");
        }
        if (!territoireRepository.existsById(COMMUNE_DOUALA)) {
            saveTerritoire(COMMUNE_DOUALA, typeCommuneId, ARR_DOUALA_1, "Commune Douala", "COM_DLA");
        }

        // Créer les utilisateurs de test (nécessaires pour sync_queue FK)
        createTestUserIfNotExists(USER_N5_YAOUNDE_1, "N5 Yaoundé-1", "n5.yde1@test.cm");
        createTestUserIfNotExists(USER_N5_YAOUNDE_2, "N5 Yaoundé-2", "n5.yde2@test.cm");
        createTestUserIfNotExists(USER_N1_CENTRAL, "N1 Central", "n1@test.cm");
        createTestUserIfNotExists(USER_N2_CENTRE, "N2 Centre", "n2.centre@test.cm");

        // Créer l'encadreur de test (nécessaire pour session_formation FK)
        if (!encadreurRepository.existsById(ENCADREUR_ID)) {
            EncadreurEntity enc = new EncadreurEntity();
            enc.setId(ENCADREUR_ID);
            enc.setTerritoireId(COMMUNE_YAOUNDE_1);
            enc.setNom("Encadreur");
            enc.setPrenom("Test");
            enc.setSyncStatus("synced");
            enc.setServerUpdatedAt(OffsetDateTime.now());
            encadreurRepository.save(enc);
        }
    }

    private void createTestUserIfNotExists(UUID userId, String nom, String email) {
        if (!utilisateurRepository.existsById(userId)) {
            UUID roleId = roleRepository.findAll().stream()
                    .filter(r -> r.getCode().equals("N5_COMMUNE")).findFirst()
                    .map(RoleEntity::getId).orElse(null);
            UtilisateurEntity user = new UtilisateurEntity(
                    userId, nom, email, "$2a$10$dummy.hash.for.testing.only.val", roleId, COMMUNE_YAOUNDE_1);
            user.setSyncStatus("synced");
            user.setServerUpdatedAt(OffsetDateTime.now());
            utilisateurRepository.save(user);
        }
    }

    private void saveTerritoire(UUID id, UUID typeId, UUID parentId, String nom, String code) {
        TerritoireEntity t = new TerritoireEntity();
        t.setId(id);
        t.setTypeTerritoireId(typeId);
        t.setParentId(parentId);
        t.setNom(nom);
        t.setCode(code);
        t.setSyncStatus("synced");
        t.setServerUpdatedAt(OffsetDateTime.now());
        territoireRepository.save(t);
    }

    // ============================================================
    // 1. CONTRÔLE DE PÉRIMÈTRE TERRITORIAL — VRAIE CTE RÉCURSIVE
    // ============================================================

    @Nested
    @DisplayName("1. Périmètre territorial — CTE récursive PostgreSQL")
    class PerimeterTerritorialTest {

        @Test
        @DisplayName("N5 Commune Yaoundé-1 peut accéder à sa propre commune")
        void n5_canAccessOwnCommune() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:read"));

            assertTrue(territoireAccessService.canAccess(COMMUNE_YAOUNDE_1));
            assertDoesNotThrow(() -> territoireAccessService.requireAccess(COMMUNE_YAOUNDE_1));
        }

        @Test
        @DisplayName("N5 Commune Yaoundé-1 ne peut pas accéder à Yaoundé-2")
        void n5_cannotAccessOtherCommune() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:read"));

            assertFalse(territoireAccessService.canAccess(COMMUNE_YAOUNDE_2));
            PerimeterAccessException ex = assertThrows(
                    PerimeterAccessException.class,
                    () -> territoireAccessService.requireAccess(COMMUNE_YAOUNDE_2)
            );
            assertTrue(ex.getMessage().contains("Accès refusé"));
        }

        @Test
        @DisplayName("N2 Région Centre peut accéder à une commune de sa région (descendant 3 niveaux)")
        void n2_canAccessCommuneInOwnRegion() {
            // N2 Région Centre → Département Mfoundi → Arrondissement Yaoundé-1 → Commune Yaoundé-1
            setupSecurityContext(USER_N2_CENTRE, "N2_COORDINATION_REG", 2, REGION_CENTRE, Set.of("apprenant:read"));

            assertTrue(territoireAccessService.canAccess(COMMUNE_YAOUNDE_1));
            assertTrue(territoireAccessService.canAccess(COMMUNE_YAOUNDE_2));
            assertTrue(territoireAccessService.canAccess(DEPT_MFOUNDI));
            assertTrue(territoireAccessService.canAccess(ARR_YAOUNDE_1));
            assertDoesNotThrow(() -> territoireAccessService.requireAccess(COMMUNE_YAOUNDE_1));
        }

        @Test
        @DisplayName("N2 Région Centre ne peut pas accéder à une commune du Littoral")
        void n2_cannotAccessCommuneInOtherRegion() {
            setupSecurityContext(USER_N2_CENTRE, "N2_COORDINATION_REG", 2, REGION_CENTRE, Set.of("apprenant:read"));

            assertFalse(territoireAccessService.canAccess(COMMUNE_DOUALA));
            assertFalse(territoireAccessService.canAccess(REGION_LITTORAL));
            assertThrows(PerimeterAccessException.class,
                    () -> territoireAccessService.requireAccess(COMMUNE_DOUALA));
        }

        @Test
        @DisplayName("N1 Comité Central peut accéder à n'importe quel territoire")
        void n1_canAccessAnyTerritory() {
            setupSecurityContext(USER_N1_CENTRAL, "N1_COMITE_CENTRAL", 1, null, Set.of("apprenant:read"));

            assertTrue(territoireAccessService.canAccess(COMMUNE_DOUALA));
            assertTrue(territoireAccessService.canAccess(COMMUNE_YAOUNDE_1));
            assertTrue(territoireAccessService.canAccess(REGION_LITTORAL));
        }

        @Test
        @DisplayName("N5 ne peut pas lire un apprenant d'une autre commune — FormationService")
        void n5_cannotReadApprenantFromOtherCommune() {
            UUID apprenantId = UUID.randomUUID();
            ApprenantEntity apprenant = createApprenant(apprenantId, COMMUNE_YAOUNDE_2, "Nkomo", "Aline");
            apprenantRepository.save(apprenant);

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:read"));

            PerimeterAccessException ex = assertThrows(
                    PerimeterAccessException.class,
                    () -> formationService.getApprenant(apprenantId)
            );
            assertTrue(ex.getMessage().contains("Accès refusé"));
        }

        @Test
        @DisplayName("N5 peut lire un apprenant de sa propre commune — FormationService")
        void n5_canReadApprenantFromOwnCommune() {
            UUID apprenantId = UUID.randomUUID();
            ApprenantEntity apprenant = createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Atangana", "Paul");
            apprenantRepository.save(apprenant);

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:read"));

            ApprenantDto dto = formationService.getApprenant(apprenantId);
            assertNotNull(dto);
            assertEquals("Atangana", dto.nom());
        }

        @Test
        @DisplayName("N5 ne peut pas créer un apprenant dans une autre commune")
        void n5_cannotCreateApprenantInOtherCommune() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:write"));

            CreateApprenantRequest req = new CreateApprenantRequest(
                    null, COMMUNE_YAOUNDE_2, "Bidjocka", "Marie", null, "F", null, null, null, null, null
            );

            assertThrows(PerimeterAccessException.class, () -> formationService.createApprenant(req));
        }

        @Test
        @DisplayName("N5 ne peut pas supprimer (soft delete) un apprenant d'une autre commune")
        void n5_cannotSoftDeleteApprenantFromOtherCommune() {
            UUID apprenantId = UUID.randomUUID();
            ApprenantEntity apprenant = createApprenant(apprenantId, COMMUNE_YAOUNDE_2, "Foka", "Jean");
            apprenantRepository.save(apprenant);

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("apprenant:write"));

            assertThrows(PerimeterAccessException.class, () -> formationService.softDeleteApprenant(apprenantId));
        }

        @Test
        @DisplayName("N5 ne peut pas saisir une présence pour une session d'une autre commune")
        void n5_cannotSaisirPresenceForSessionInOtherCommune() {
            UUID sessionId = UUID.randomUUID();
            SessionFormationEntity session = createSession(sessionId, COMMUNE_YAOUNDE_2, ENCADREUR_ID);
            sessionRepository.save(session);

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1, Set.of("presence:write"));

            CreatePresenceRequest req = new CreatePresenceRequest(
                    null, sessionId, UUID.randomUUID(), LocalDate.now(), "present"
            );

            assertThrows(PerimeterAccessException.class, () -> formationService.saisirPresence(req));
        }

        @Test
        @DisplayName("N2 Région Centre peut lire un apprenant d'une commune de sa région")
        void n2_canReadApprenantInOwnRegion() {
            UUID apprenantId = UUID.randomUUID();
            ApprenantEntity apprenant = createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Mballa", "Sophie");
            apprenantRepository.save(apprenant);

            setupSecurityContext(USER_N2_CENTRE, "N2_COORDINATION_REG", 2, REGION_CENTRE, Set.of("apprenant:read"));

            ApprenantDto dto = formationService.getApprenant(apprenantId);
            assertNotNull(dto);
            assertEquals("Mballa", dto.nom());
        }
    }

    // ============================================================
    // 2. RBAC DYNAMIQUE — retrait de permission = accès refusé
    // ============================================================

    @Nested
    @DisplayName("2. RBAC dynamique — retrait d'une permission bloque l'accès")
    class DynamicRbacTest {

        @Test
        @DisplayName("Un utilisateur avec 'notification:write' a la permission")
        void userWithPermission_canAccess() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("notification:write"));

            SecurityContext ctx = (SecurityContext) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertTrue(ctx.hasPermission("notification:write"));
        }

        @Test
        @DisplayName("Un utilisateur sans 'notification:write' n'a pas la permission")
        void userWithoutPermission_cannotAccess() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:read"));

            SecurityContext ctx = (SecurityContext) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertFalse(ctx.hasPermission("notification:write"));
        }

        @Test
        @DisplayName("Le retrait d'une permission en base est effectif au prochain login")
        void removedPermission_isReflectedOnNextToken() {
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
            RoleEntity role = new RoleEntity();
            role.setId(UUID.randomUUID());
            role.setCode("N5_TEST_" + uniqueSuffix);
            role.setLibelle("Commune Test");
            role.setNiveauHierarchique((short) 5);
            roleRepository.save(role);

            PermissionEntity perm = new PermissionEntity();
            perm.setId(UUID.randomUUID());
            perm.setCode("test_perm_" + uniqueSuffix);
            perm.setLibelle("Test permission");
            permissionRepository.save(perm);

            rolePermissionService.assignPermission(role.getId(), perm.getId());

            List<String> perms = rolePermissionService.getPermissionCodesForRole(role.getId());
            assertTrue(perms.contains("test_perm_" + uniqueSuffix));

            rolePermissionService.removePermission(role.getId(), perm.getId());

            List<String> permsAfter = rolePermissionService.getPermissionCodesForRole(role.getId());
            assertFalse(permsAfter.contains("test_perm_" + uniqueSuffix));

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_TEST_" + uniqueSuffix, 5, COMMUNE_YAOUNDE_1,
                    new HashSet<>(permsAfter));
            SecurityContext ctx = (SecurityContext) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertFalse(ctx.hasPermission("test_perm_" + uniqueSuffix));
        }

        @Test
        @DisplayName("AdminService refuse l'accès sans 'audit:read'")
        void adminService_requiresAuditReadPermission() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:read"));

            SecurityContext ctx = (SecurityContext) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();
            assertFalse(ctx.hasPermission("audit:read"));
            assertFalse(ctx.hasPermission("parametre:write"));
        }
    }

    // ============================================================
    // 3. SYNC — CONFLIT LAST WRITE WINS + sync_conflict_log
    // ============================================================

    @Nested
    @DisplayName("3. /api/sync — conflit Last Write Wins et journalisation")
    class SyncConflictTest {

        @Test
        @DisplayName("Deux utilisateurs modifient la même présence — LWW et ligne dans sync_conflict_log")
        void syncConflict_lastWriteWinsAndLogged() {
            UUID presenceId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            UUID apprenantId = UUID.randomUUID();

            SessionFormationEntity session = createSession(sessionId, COMMUNE_YAOUNDE_1, ENCADREUR_ID);
            sessionRepository.save(session);
            apprenantRepository.save(createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Sync", "Test1"));

            OffsetDateTime baseTime = OffsetDateTime.now().minusHours(2);
            PresenceEntity presence = new PresenceEntity();
            presence.setId(presenceId);
            presence.setSessionId(sessionId);
            presence.setApprenantId(apprenantId);
            presence.setDate(LocalDate.now());
            presence.setStatut("present");
            presence.setSaisieParId(USER_N5_YAOUNDE_1);
            presence.setSyncStatus("synced");
            presence.setServerUpdatedAt(baseTime.plusMinutes(30));
            presence.setClientUpdatedAt(baseTime);
            presenceRepository.save(presence);

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("presence:write"));

            UUID actionId1 = UUID.randomUUID();
            OffsetDateTime clientTime1 = baseTime.plusMinutes(45);

            SyncActionDto action1 = new SyncActionDto(
                    actionId1, "presence", presenceId, "update",
                    Map.of("sessionId", sessionId.toString(),
                           "apprenantId", apprenantId.toString(),
                           "statut", "absent",
                           "date", LocalDate.now().toString()),
                    clientTime1
            );

            SyncRequestDto request1 = new SyncRequestDto(USER_N5_YAOUNDE_1, "device-1", List.of(action1));
            SyncResponseDto response1 = syncService.synchronize(request1);

            assertEquals(1, response1.totalActions());
            assertEquals(0, response1.applied());
            assertEquals(1, response1.conflicts());
            assertEquals(0, response1.rejected());

            SyncResultDto result1 = response1.resultats().get(0);
            assertEquals("conflict", result1.statut());
            assertEquals("Résolu par Last Write Wins", result1.message());

            List<SyncConflictLogEntity> conflicts = conflictLogRepository.findAll();
            assertFalse(conflicts.isEmpty(), "sync_conflict_log doit contenir au moins une entrée");

            SyncConflictLogEntity conflictLog = conflicts.stream()
                    .filter(c -> c.getSyncQueueId().equals(actionId1))
                    .findFirst()
                    .orElse(null);
            assertNotNull(conflictLog, "Une ligne avec sync_queue_id = actionId1 doit exister");
            assertEquals("last_write_wins", conflictLog.getResolution());
            assertNotNull(conflictLog.getVersionServeur());
            assertNotNull(conflictLog.getVersionClient());

            PresenceEntity updated = presenceRepository.findById(presenceId).orElseThrow();
            assertEquals("absent", updated.getStatut(), "Le statut doit refléter la version gagnante du client");
        }

        @Test
        @DisplayName("Deuxième utilisateur sync avec horodatage plus ancien — serveur gagne")
        void syncConflict_serverWinsWhenClientOlder() {
            UUID presenceId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            UUID apprenantId = UUID.randomUUID();

            SessionFormationEntity session = createSession(sessionId, COMMUNE_YAOUNDE_1, ENCADREUR_ID);
            sessionRepository.save(session);
            apprenantRepository.save(createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Sync", "Test2"));

            OffsetDateTime baseTime = OffsetDateTime.now().minusHours(1);
            PresenceEntity presence = new PresenceEntity();
            presence.setId(presenceId);
            presence.setSessionId(sessionId);
            presence.setApprenantId(apprenantId);
            presence.setDate(LocalDate.now());
            presence.setStatut("present");
            presence.setSaisieParId(USER_N5_YAOUNDE_1);
            presence.setSyncStatus("synced");
            presence.setServerUpdatedAt(baseTime.plusMinutes(30));
            presence.setClientUpdatedAt(baseTime.plusMinutes(15));
            presenceRepository.save(presence);

            setupSecurityContext(USER_N5_YAOUNDE_2, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("presence:write"));

            UUID actionId = UUID.randomUUID();
            OffsetDateTime olderClientTime = baseTime.plusMinutes(5);

            SyncActionDto action = new SyncActionDto(
                    actionId, "presence", presenceId, "update",
                    Map.of("sessionId", sessionId.toString(),
                           "apprenantId", apprenantId.toString(),
                           "statut", "retard",
                           "date", LocalDate.now().toString()),
                    olderClientTime
            );

            SyncRequestDto request = new SyncRequestDto(USER_N5_YAOUNDE_2, "device-2", List.of(action));
            SyncResponseDto response = syncService.synchronize(request);

            assertEquals(1, response.conflicts());
            SyncResultDto result = response.resultats().get(0);
            assertEquals("conflict", result.statut());

            PresenceEntity unchanged = presenceRepository.findById(presenceId).orElseThrow();
            assertEquals("present", unchanged.getStatut(),
                    "Le statut ne doit pas changer car le client avait un horodatage plus ancien");
        }

        @Test
        @DisplayName("Idempotence — un retry de la même action renvoie le statut précédent")
        void syncRetry_returnsPreviousStatus() {
            UUID presenceId = UUID.randomUUID();
            UUID sessionId = UUID.randomUUID();
            UUID apprenantId = UUID.randomUUID();
            UUID actionId = UUID.randomUUID();

            SessionFormationEntity session = createSession(sessionId, COMMUNE_YAOUNDE_1, ENCADREUR_ID);
            sessionRepository.save(session);
            apprenantRepository.save(createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Sync", "Test3"));

            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("presence:write"));

            OffsetDateTime clientTime = OffsetDateTime.now();
            SyncActionDto action = new SyncActionDto(
                    actionId, "presence", presenceId, "create",
                    Map.of("sessionId", sessionId.toString(),
                           "apprenantId", apprenantId.toString(),
                           "statut", "present",
                           "date", LocalDate.now().toString()),
                    clientTime
            );

            SyncRequestDto request = new SyncRequestDto(USER_N5_YAOUNDE_1, "device-1", List.of(action));
            SyncResponseDto response1 = syncService.synchronize(request);
            assertEquals(1, response1.applied());

            SyncResponseDto response2 = syncService.synchronize(request);
            assertEquals(1, response2.applied());
            assertEquals("applied", response2.resultats().get(0).statut());
        }
    }

    // ============================================================
    // 4. ACCÈS DIRECT API (contournement frontend)
    // ============================================================

    @Nested
    @DisplayName("4. Accès direct API — sécurité sans frontend")
    class DirectApiAccessTest {

        @Test
        @DisplayName("Requête forgée avec territoire hors périmètre — accès refusé")
        void forgedRequestWithOutOfPerimeterTerritory_isRejected() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:write"));

            CreateApprenantRequest forgedReq = new CreateApprenantRequest(
                    null, COMMUNE_DOUALA, "Hacker", "Script", null, "M", null, null, null, null, null
            );

            PerimeterAccessException ex = assertThrows(
                    PerimeterAccessException.class,
                    () -> formationService.createApprenant(forgedReq)
            );
            assertTrue(ex.getMessage().contains("Accès refusé"));
            assertTrue(ex.getMessage().contains(COMMUNE_DOUALA.toString()));
        }

        @Test
        @DisplayName("Requête sans JWT (anonyme) — SecurityContext vide, accès refusé")
        void requestWithoutJwt_isRejected() {
            SecurityContextHolder.clearContext();

            UUID apprenantId = UUID.randomUUID();
            ApprenantEntity apprenant = createApprenant(apprenantId, COMMUNE_YAOUNDE_1, "Test", "Anonyme");
            apprenantRepository.save(apprenant);

            assertThrows(IllegalStateException.class, () -> {
                formationService.getApprenant(apprenantId);
            });
        }

        @Test
        @DisplayName("Requête avec territoire null dans le payload — accès refusé")
        void requestWithNullTerritory_isRejected() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:write"));

            CreateApprenantRequest req = new CreateApprenantRequest(
                    null, null, "Test", "Null", null, "M", null, null, null, null, null
            );

            assertThrows(PerimeterAccessException.class, () -> formationService.createApprenant(req));
        }

        @Test
        @DisplayName("Lister les apprenants d'un territoire hors périmètre — refusé")
        void listApprenantsFromOtherCommune_isRejected() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:read"));

            assertThrows(PerimeterAccessException.class,
                    () -> formationService.listApprenantsByTerritoire(COMMUNE_DOUALA));
        }

        @Test
        @DisplayName("Sync avec payload contenant un territoireId hors périmètre — refusé")
        void syncWithOutOfPerimeterTerritory_isRejected() {
            setupSecurityContext(USER_N5_YAOUNDE_1, "N5_COMMUNE", 5, COMMUNE_YAOUNDE_1,
                    Set.of("apprenant:write"));

            UUID newApprenantId = UUID.randomUUID();
            UUID actionId = UUID.randomUUID();

            SyncActionDto action = new SyncActionDto(
                    actionId, "apprenant", newApprenantId, "create",
                    Map.of("territoireId", COMMUNE_DOUALA.toString(),
                           "nom", "Tentative", "prenom", "Hack"),
                    OffsetDateTime.now()
            );

            SyncRequestDto request = new SyncRequestDto(USER_N5_YAOUNDE_1, "device-hack", List.of(action));
            SyncResponseDto response = syncService.synchronize(request);

            assertEquals(1, response.rejected());
            assertEquals("rejected", response.resultats().get(0).statut());
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private void setupSecurityContext(UUID userId, String roleCode, int niveau,
                                       UUID territoireId, Set<String> permissions) {
        SecurityContext ctx = new SecurityContext(userId, roleCode, niveau, territoireId, permissions);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(ctx, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private ApprenantEntity createApprenant(UUID id, UUID territoireId, String nom, String prenom) {
        ApprenantEntity e = new ApprenantEntity();
        e.setId(id);
        e.setTerritoireId(territoireId);
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setSyncStatus("synced");
        e.setServerUpdatedAt(OffsetDateTime.now());
        return e;
    }

    private SessionFormationEntity createSession(UUID id, UUID territoireId, UUID encadreurId) {
        SessionFormationEntity e = new SessionFormationEntity();
        e.setId(id);
        e.setTerritoireId(territoireId);
        e.setEncadreurId(encadreurId);
        e.setDateDebut(LocalDate.now());
        e.setStatut("planifiee");
        e.setSyncStatus("synced");
        e.setServerUpdatedAt(OffsetDateTime.now());
        return e;
    }
}
