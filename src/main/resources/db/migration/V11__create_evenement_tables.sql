-- =====================================================================
-- M5 — Événements (gestion des événements et projections)
-- Table: evenement
-- =====================================================================

CREATE TABLE IF NOT EXISTS evenement (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre           TEXT NOT NULL,
    description     TEXT,
    type            TEXT NOT NULL DEFAULT 'projection',
    date_debut      TIMESTAMPTZ NOT NULL,
    date_fin        TIMESTAMPTZ,
    lieu            TEXT,
    adresse         TEXT,
    commune_id      UUID REFERENCES territoire(id),
    image_url       TEXT,
    capacite        INTEGER,
    statut          TEXT NOT NULL DEFAULT 'programme',
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_evenement_statut ON evenement(statut);
CREATE INDEX IF NOT EXISTS idx_evenement_date_debut ON evenement(date_debut);
CREATE INDEX IF NOT EXISTS idx_evenement_commune ON evenement(commune_id);

GRANT ALL PRIVILEGES ON TABLE evenement TO kct_dev;
