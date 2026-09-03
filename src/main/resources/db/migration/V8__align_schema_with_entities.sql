-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration V8: alignement schéma/entités
--
-- Ajoute les colonnes manquantes découvertes lors des tests d'intégration
-- contre PostgreSQL réel. Ces colonnes existent dans les entités JPA mais
-- n'avaient pas été créées dans les migrations V1-V7.
--
-- Colonnes concernées:
--   1. type_territoire.libelle  (VARCHAR NOT NULL)
--   2. statut_commune.libelle   (VARCHAR NOT NULL)
--   3. territoire.deleted_at     (TIMESTAMPTZ NULL — suppression douce)
--   4. territoire.statut_commune_id (UUID NULL FK vers statut_commune)
-- =====================================================================

-- 1. type_territoire.libelle
ALTER TABLE type_territoire
    ADD COLUMN IF NOT EXISTS libelle VARCHAR(255) NOT NULL DEFAULT '';

-- Mettre à jour les libellés des types de territoire existants
UPDATE type_territoire SET libelle = 'National'      WHERE code = 'national'       AND libelle = '';
UPDATE type_territoire SET libelle = 'Région'        WHERE code = 'region'         AND libelle = '';
UPDATE type_territoire SET libelle = 'Département'   WHERE code = 'departement'    AND libelle = '';
UPDATE type_territoire SET libelle = 'Arrondissement' WHERE code = 'arrondissement' AND libelle = '';
UPDATE type_territoire SET libelle = 'Commune'       WHERE code = 'commune'        AND libelle = '';

-- 2. statut_commune.libelle
ALTER TABLE statut_commune
    ADD COLUMN IF NOT EXISTS libelle VARCHAR(255) NOT NULL DEFAULT '';

-- Mettre à jour les libellés des statuts de commune existants
UPDATE statut_commune SET libelle = 'Terminée'      WHERE code = 'terminee'      AND libelle = '';
UPDATE statut_commune SET libelle = 'En cours'      WHERE code = 'en_cours'      AND libelle = '';
UPDATE statut_commune SET libelle = 'Non démarrée'  WHERE code = 'non_demarree'  AND libelle = '';
UPDATE statut_commune SET libelle = 'Suspendue'     WHERE code = 'suspendue'     AND libelle = '';

-- 3. territoire.deleted_at (suppression douce)
ALTER TABLE territoire
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ NULL;

-- 4. territoire.statut_commune_id (FK vers statut_commune)
ALTER TABLE territoire
    ADD COLUMN IF NOT EXISTS statut_commune_id UUID NULL REFERENCES statut_commune(id);

CREATE INDEX IF NOT EXISTS idx_territoire_statut_commune ON territoire (statut_commune_id);
CREATE INDEX IF NOT EXISTS idx_territoire_deleted_at     ON territoire (deleted_at)
    WHERE deleted_at IS NOT NULL;
