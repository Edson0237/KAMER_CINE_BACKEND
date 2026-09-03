-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration initiale du noyau V1
-- Modules: M0 (Administration), M1 (IAM), M2 (Territoire)
-- Règles: UUID partout, pas d'ENUM, sync columns sur tables terrain,
--         suppression douce, audit_log polymorphe.
-- =====================================================================

-- Activer l'extension pgcrypto pour gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =====================================================================
-- M0 — AUDIT LOG (transverse, consommé par tous les modules)
-- =====================================================================
CREATE TABLE audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id  UUID NULL,
    action          TEXT NOT NULL,
    entite_type     TEXT NOT NULL,
    entite_id       UUID NOT NULL,
    date            TIMESTAMPTZ NOT NULL DEFAULT now(),
    details         JSONB NULL
);

CREATE INDEX idx_audit_log_utilisateur ON audit_log (utilisateur_id);
CREATE INDEX idx_audit_log_entite      ON audit_log (entite_type, entite_id);
CREATE INDEX idx_audit_log_date        ON audit_log (date DESC);

-- =====================================================================
-- M1 — IAM : role, permission, role_permission, utilisateur
-- =====================================================================

-- Table de référence des rôles (jamais d'ENUM en base)
CREATE TABLE role (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                TEXT NOT NULL UNIQUE,
    libelle             TEXT NOT NULL,
    niveau_hierarchique SMALLINT NOT NULL
);

-- Table de référence des permissions (granularité fine)
CREATE TABLE permission (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code    TEXT NOT NULL UNIQUE,
    libelle TEXT NOT NULL
);

-- Table de liaison N-N rôle ↔ permission
CREATE TABLE role_permission (
    role_id       UUID NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Table utilisateur avec colonnes de sync (modifiable sur le terrain)
CREATE TABLE utilisateur (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom               TEXT NOT NULL,
    email             TEXT NOT NULL UNIQUE,
    password_hash     TEXT NOT NULL,
    role_id           UUID NOT NULL REFERENCES role(id),
    territoire_id     UUID NULL,
    telephone         TEXT NULL,
    actif             BOOLEAN NOT NULL DEFAULT true,
    metadata          JSONB NULL,
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_utilisateur_role       ON utilisateur (role_id);
CREATE INDEX idx_utilisateur_territoire ON utilisateur (territoire_id);

-- =====================================================================
-- M2 — TERRITOIRE : type_territoire, statut_commune, territoire
-- =====================================================================

-- Table de référence des types de territoire (national/région/dépt/arrond/commune)
CREATE TABLE type_territoire (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code    TEXT NOT NULL UNIQUE,
    niveau  SMALLINT NOT NULL
);

-- Table de référence des statuts de commune (couleurs carte interactive)
CREATE TABLE statut_commune (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       TEXT NOT NULL UNIQUE,
    couleur_hex TEXT NOT NULL
);

-- Table territoire auto-référencée (un seul modèle pour 5 niveaux)
CREATE TABLE territoire (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_territoire_id  UUID NOT NULL REFERENCES type_territoire(id),
    parent_id           UUID NULL REFERENCES territoire(id),
    nom                 TEXT NOT NULL,
    code                TEXT NOT NULL,
    statut_formation_id UUID NULL REFERENCES statut_commune(id),
    metadata            JSONB NULL,
    server_updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at   TIMESTAMPTZ NULL,
    sync_status         TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_territoire_type   ON territoire (type_territoire_id);
CREATE INDEX idx_territoire_parent ON territoire (parent_id);

-- =====================================================================
-- M0 — ADMINISTRATION : parametre_systeme, feature_flag, integration_externe,
--       sauvegarde, import_job, incident_technique
-- =====================================================================

CREATE TABLE parametre_systeme (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cle                  TEXT NOT NULL UNIQUE,
    valeur               TEXT NULL,
    type                 TEXT NOT NULL DEFAULT 'string',
    description          TEXT NULL,
    modifiable_par_role_id UUID NULL REFERENCES role(id)
);

CREATE TABLE feature_flag (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           TEXT NOT NULL UNIQUE,
    libelle        TEXT NOT NULL,
    actif          BOOLEAN NOT NULL DEFAULT false,
    version_cible  TEXT NULL,
    territoire_id  UUID NULL REFERENCES territoire(id)
);

CREATE INDEX idx_feature_flag_territoire ON feature_flag (territoire_id);

CREATE TABLE integration_externe (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                  TEXT NOT NULL,
    config                JSONB NULL,
    actif                 BOOLEAN NOT NULL DEFAULT false,
    derniere_verification TIMESTAMPTZ NULL
);

CREATE TABLE sauvegarde (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date_declenchement     TIMESTAMPTZ NOT NULL DEFAULT now(),
    type                   TEXT NOT NULL DEFAULT 'auto',
    statut                 TEXT NOT NULL DEFAULT 'succes',
    taille_mo              NUMERIC NULL,
    date_test_restauration TIMESTAMPTZ NULL
);

CREATE TABLE import_job (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_entite         TEXT NOT NULL,
    fichier_source      TEXT NOT NULL,
    statut              TEXT NOT NULL DEFAULT 'en_cours',
    nb_lignes_traitees  INT NOT NULL DEFAULT 0,
    nb_erreurs          INT NOT NULL DEFAULT 0,
    utilisateur_id      UUID NOT NULL REFERENCES utilisateur(id),
    date                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_import_job_utilisateur ON import_job (utilisateur_id);

CREATE TABLE incident_technique (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type            TEXT NOT NULL,
    description     TEXT NULL,
    date_debut      TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_resolution TIMESTAMPTZ NULL,
    statut          TEXT NOT NULL DEFAULT 'ouvert'
);
