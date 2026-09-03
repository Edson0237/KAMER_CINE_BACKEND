-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration M3 Formation
-- Tables: apprenant, encadreur, session_formation, inscription_session,
--         presence, resultat_examen, attestation
-- Règles: UUID, sync columns, soft delete, JSONB metadata
-- =====================================================================

-- Apprenant — fiche principale, modifiable sur le terrain (sync)
CREATE TABLE apprenant (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    territoire_id     UUID NOT NULL REFERENCES territoire(id),
    nom               TEXT NOT NULL,
    prenom            TEXT NOT NULL,
    date_naissance    DATE NULL,
    sexe              TEXT NULL,
    telephone         TEXT NULL,
    photo_url         TEXT NULL,
    competences       JSONB NULL,
    portfolio         JSONB NULL,
    metadata          JSONB NULL,
    deleted_at        TIMESTAMPTZ NULL,
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_apprenant_territoire ON apprenant (territoire_id);

-- Encadreur — formateur, modifiable sur le terrain (sync)
CREATE TABLE encadreur (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    territoire_id     UUID NOT NULL REFERENCES territoire(id),
    nom               TEXT NOT NULL,
    prenom            TEXT NOT NULL,
    telephone         TEXT NULL,
    specialite        TEXT NULL,
    disponibilite     TEXT NULL,
    evaluation_moyenne NUMERIC NULL,
    photo_url         TEXT NULL,
    metadata          JSONB NULL,
    deleted_at        TIMESTAMPTZ NULL,
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_encadreur_territoire ON encadreur (territoire_id);

-- Session de formation
CREATE TABLE session_formation (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    territoire_id     UUID NOT NULL REFERENCES territoire(id),
    encadreur_id      UUID NOT NULL REFERENCES encadreur(id),
    date_debut        DATE NOT NULL,
    date_fin          DATE NULL,
    lieu              TEXT NULL,
    programme         TEXT NULL,
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_session_territoire ON session_formation (territoire_id);
CREATE INDEX idx_session_encadreur  ON session_formation (encadreur_id);

-- Inscription session (table de liaison N-N)
CREATE TABLE inscription_session (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id   UUID NOT NULL REFERENCES session_formation(id),
    apprenant_id UUID NOT NULL REFERENCES apprenant(id),
    UNIQUE (session_id, apprenant_id)
);

CREATE INDEX idx_inscription_session   ON inscription_session (session_id);
CREATE INDEX idx_inscription_apprenant ON inscription_session (apprenant_id);

-- Présence — table la plus sollicitée hors-ligne
CREATE TABLE presence (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID NOT NULL REFERENCES session_formation(id),
    apprenant_id      UUID NOT NULL REFERENCES apprenant(id),
    date              DATE NOT NULL,
    statut            TEXT NOT NULL DEFAULT 'present',
    saisie_par_id     UUID NOT NULL REFERENCES utilisateur(id),
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_presence_session   ON presence (session_id);
CREATE INDEX idx_presence_apprenant ON presence (apprenant_id);
CREATE INDEX idx_presence_date      ON presence (date);

-- Résultat d'examen
CREATE TABLE resultat_examen (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id        UUID NOT NULL REFERENCES session_formation(id),
    apprenant_id      UUID NOT NULL REFERENCES apprenant(id),
    note              NUMERIC NULL,
    date_examen       DATE NOT NULL,
    server_updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    client_updated_at TIMESTAMPTZ NULL,
    sync_status       TEXT NOT NULL DEFAULT 'synced'
);

CREATE INDEX idx_resultat_session   ON resultat_examen (session_id);
CREATE INDEX idx_resultat_apprenant ON resultat_examen (apprenant_id);

-- Attestation — générée côté serveur uniquement
CREATE TABLE attestation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apprenant_id    UUID NOT NULL REFERENCES apprenant(id),
    session_id      UUID NOT NULL REFERENCES session_formation(id),
    numero          TEXT NOT NULL UNIQUE,
    date_delivrance DATE NOT NULL,
    fichier_url     TEXT NULL
);

CREATE INDEX idx_attestation_apprenant ON attestation (apprenant_id);
CREATE INDEX idx_attestation_session   ON attestation (session_id);
