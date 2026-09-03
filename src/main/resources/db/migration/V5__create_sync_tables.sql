-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration M5 Synchronisation
-- Tables: sync_queue, sync_conflict_log
-- Règles: UUID, entite_type générique, JSONB payload, resolution LWW
-- =====================================================================

-- sync_queue — file d'attente générique pour toutes les entités hors-ligne
CREATE TABLE sync_queue (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    utilisateur_id       UUID NOT NULL REFERENCES utilisateur(id),
    entite_type          TEXT NOT NULL,
    entite_id            UUID NOT NULL,
    operation            TEXT NOT NULL,
    payload              JSONB NOT NULL,
    horodatage_client    TIMESTAMPTZ NOT NULL,
    horodatage_reception TIMESTAMPTZ NULL,
    statut               TEXT NOT NULL DEFAULT 'pending',
    tentative            SMALLINT NOT NULL DEFAULT 0,
    message_erreur       TEXT NULL
);

CREATE INDEX idx_sync_queue_utilisateur ON sync_queue (utilisateur_id);
CREATE INDEX idx_sync_queue_statut      ON sync_queue (statut);
CREATE INDEX idx_sync_queue_entite      ON sync_queue (entite_type, entite_id);

-- sync_conflict_log — journal des conflits résolus (audit a posteriori)
CREATE TABLE sync_conflict_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sync_queue_id   UUID NOT NULL REFERENCES sync_queue(id),
    resolution      TEXT NOT NULL,
    date_resolution TIMESTAMPTZ NOT NULL DEFAULT now(),
    version_serveur JSONB NULL,
    version_client  JSONB NULL,
    resolu_par_id   UUID NULL REFERENCES utilisateur(id)
);

CREATE INDEX idx_sync_conflict_queue ON sync_conflict_log (sync_queue_id);
