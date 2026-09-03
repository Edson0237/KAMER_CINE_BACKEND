-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Données de référence initiales
-- Rôles (7 niveaux), permissions, types de territoire, statuts commune,
-- et un compte administrateur système par défaut.
-- =====================================================================

-- =====================================================================
-- M1 — Rôles (7 niveaux hiérarchiques)
-- =====================================================================
INSERT INTO role (code, libelle, niveau_hierarchique) VALUES
    ('N1_COMITE_CENTRAL',  'Comité Central — Niveau National',    1),
    ('N2_COORDINATION_REG', 'Coordination Régionale',              2),
    ('N3_COORDINATION_DEPT','Coordination Départementale',         3),
    ('N4_COORDINATION_ARR', 'Coordination Arrondissement',         4),
    ('N5_COMMUNE',          'Coordination Communale',              5),
    ('N6_ENCADREUR',        'Encadreur de terrain',                6),
    ('N7_APPRENANT',        'Apprenant',                           7);

-- =====================================================================
-- M1 — Permissions (granularité fine, indépendante des rôles)
-- =====================================================================
INSERT INTO permission (code, libelle) VALUES
    ('utilisateur:read',       'Consulter les utilisateurs'),
    ('utilisateur:write',      'Créer ou modifier un utilisateur'),
    ('utilisateur:delete',     'Désactiver un utilisateur'),
    ('role:read',              'Consulter les rôles et permissions'),
    ('role:write',             'Gérer les rôles et permissions'),
    ('territoire:read',        'Consulter les territoires'),
    ('territoire:write',       'Créer ou modifier un territoire'),
    ('territoire:statut',      'Changer le statut de formation d''une commune'),
    ('audit:read',             'Consulter le journal d''audit'),
    ('parametre:read',         'Consulter les paramètres système'),
    ('parametre:write',        'Modifier les paramètres système'),
    ('feature_flag:read',      'Consulter les feature flags'),
    ('feature_flag:write',     'Activer/désactiver un feature flag'),
    ('integration:write',      'Gérer les intégrations externes'),
    ('sauvegarde:read',        'Consulter l''historique des sauvegardes'),
    ('sauvegarde:trigger',     'Déclencher une sauvegarde manuelle'),
    ('import:write',           'Lancer un import en masse');

-- =====================================================================
-- M1 — Rôle ↔ Permission (le N1 a toutes les permissions)
-- =====================================================================
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.code = 'N1_COMITE_CENTRAL';

-- N5 (Commune) peut lire territoires et utilisateurs
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.code = 'N5_COMMUNE' AND p.code IN (
    'territoire:read', 'utilisateur:read', 'audit:read'
);

-- N6 (Encadreur) peut lire territoires
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.code = 'N6_ENCADREUR' AND p.code IN (
    'territoire:read'
);

-- =====================================================================
-- M2 — Types de territoire (5 niveaux administratifs)
-- =====================================================================
INSERT INTO type_territoire (code, niveau) VALUES
    ('national',      1),
    ('region',        2),
    ('departement',   3),
    ('arrondissement', 4),
    ('commune',       5);

-- =====================================================================
-- M2 — Statuts de commune (couleurs de la carte interactive)
-- =====================================================================
INSERT INTO statut_commune (code, couleur_hex) VALUES
    ('terminee',     '#3F9142'),
    ('en_cours',     '#C9A227'),
    ('non_demarree', '#B8860B'),
    ('suspendue',    '#C0392B');

-- =====================================================================
-- M2 — Territoire national (racine de la hiérarchie)
-- =====================================================================
INSERT INTO territoire (id, type_territoire_id, parent_id, nom, code, statut_formation_id, metadata)
SELECT gen_random_uuid(), tt.id, NULL, 'Cameroun', 'CMR', NULL, '{"source": "seed"}'::jsonb
FROM type_territoire tt WHERE tt.code = 'national';

-- =====================================================================
-- M0 — Paramètre système par défaut
-- =====================================================================
INSERT INTO parametre_systeme (cle, valeur, type, description, modifiable_par_role_id)
SELECT 'app.langue_defaut', 'fr', 'string', 'Langue par défaut de l''application', r.id
FROM role r WHERE r.code = 'N1_COMITE_CENTRAL';

INSERT INTO parametre_systeme (cle, valeur, type, description)
VALUES ('app.nom_organisation', 'KAMER CINÉ TALENTS', 'string', 'Nom officiel de l''organisation');

-- =====================================================================
-- M1 — Compte administrateur système par défaut
-- Mot de passe: "changeme123" (BCrypt hash — à changer dès la première connexion)
-- Hash généré avec BCrypt strength=10
-- =====================================================================
INSERT INTO utilisateur (nom, email, password_hash, role_id, territoire_id, telephone, actif, metadata)
SELECT 'Administrateur Système',
       'admin@kamercinetalents.cm',
       '$2b$10$ZC1f7NZGGQMO2ejvZqDEfeUh8V3awGJVL/6ojEkWz5XgW0iuCapf.',
       r.id,
       t.id,
       NULL,
       true,
       '{"source": "seed", "must_change_password": true}'::jsonb
FROM role r, territoire t
WHERE r.code = 'N1_COMITE_CENTRAL'
  AND t.code = 'CMR';
