-- =====================================================================
-- V10 — Rôle ADMINISTRATEUR_SYSTEME distinct de N1_COMITE_CENTRAL
--
-- Crée un rôle d'administration système (niveau 0, hors hiérarchie
-- territoriale) avec les permissions M0 (audit, paramètres, feature flags,
-- intégrations, sauvegardes, imports).
-- Met à jour le compte admin par défaut pour utiliser ce rôle.
-- Un utilisateur peut détenir BOTH roles (N1 + ADMIN_SYS) séparément.
-- =====================================================================

-- 1. Créer le rôle ADMINISTRATEUR_SYSTEME (niveau 0 = hors périmètre territorial)
INSERT INTO role (code, libelle, niveau_hierarchique)
VALUES ('ADMINISTRATEUR_SYSTEME', 'Administrateur Système', 0)
ON CONFLICT (code) DO NOTHING;

-- 2. Assigner les permissions M0 (administration système) à ce rôle
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.code = 'ADMINISTRATEUR_SYSTEME'
  AND p.code IN (
    'audit:read',
    'parametre:read',
    'parametre:write',
    'feature_flag:read',
    'feature_flag:write',
    'integration:write',
    'sauvegarde:read',
    'sauvegarde:trigger',
    'import:write',
    'utilisateur:read',
    'utilisateur:write',
    'utilisateur:delete',
    'role:read',
    'role:write'
  )
ON CONFLICT DO NOTHING;

-- 3. Retirer les permissions M0 exclusivement système de N1_COMITE_CENTRAL
--    (N1 garde ses permissions territoriales mais perd l'exclusivité sur M0)
--    N1 garde: audit:read (pour consultation), mais perd parametre:write,
--    feature_flag:write, integration:write, sauvegarde:trigger, import:write
DELETE FROM role_permission
WHERE role_id = (SELECT id FROM role WHERE code = 'N1_COMITE_CENTRAL')
  AND permission_id IN (
    SELECT id FROM permission WHERE code IN (
      'parametre:write',
      'feature_flag:write',
      'integration:write',
      'sauvegarde:trigger',
      'import:write'
    )
  );

-- 4. Réassigner le compte admin par défaut au rôle ADMINISTRATEUR_SYSTEME
UPDATE utilisateur
SET role_id = (SELECT id FROM role WHERE code = 'ADMINISTRATEUR_SYSTEME')
WHERE email = 'admin@kamercinetalents.cm'
  AND metadata->>'source' = 'seed';

-- 5. Mettre à jour le paramètre système modifiable_par_role_id
UPDATE parametre_systeme
SET modifiable_par_role_id = (SELECT id FROM role WHERE code = 'ADMINISTRATEUR_SYSTEME')
WHERE cle = 'app.langue_defaut';
