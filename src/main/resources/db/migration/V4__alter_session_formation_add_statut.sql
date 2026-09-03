-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration V4: session_formation statut
-- Ajoute la colonne statut pour suivre le cycle de vie d'une session
-- (planifiee, en_cours, cloturee) — nécessaire pour le calcul du
-- taux de réussite à la clôture.
-- =====================================================================

ALTER TABLE session_formation
    ADD COLUMN statut TEXT NOT NULL DEFAULT 'planifiee';
