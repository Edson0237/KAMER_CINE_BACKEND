-- =====================================================================
-- KCTM — Module Écosystème (site public)
-- Tables: actualite_publique, faq_item, membre_equipe, partenaire,
--         candidature_publique, contact_message
-- Règles: UUID partout, pas d'ENUM, suppression douce sur actualités.
-- =====================================================================

-- =====================================================================
-- Actualités publiques (vitrine du site)
-- =====================================================================
CREATE TABLE actualite_publique (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titre       TEXT NOT NULL,
    contenu     TEXT NOT NULL,
    image_url   TEXT NULL,
    date_publication TIMESTAMPTZ NOT NULL DEFAULT now(),
    statut      TEXT NOT NULL DEFAULT 'brouillon',
    deleted_at  TIMESTAMPTZ NULL
);

CREATE INDEX idx_actualite_statut ON actualite_publique (statut);
CREATE INDEX idx_actualite_date   ON actualite_publique (date_publication DESC);

-- =====================================================================
-- FAQ — questions fréquentes groupées par catégorie
-- =====================================================================
CREATE TABLE faq_item (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question    TEXT NOT NULL,
    reponse     TEXT NOT NULL,
    categorie   TEXT NOT NULL,
    ordre       INT NOT NULL DEFAULT 0,
    actif       BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_faq_categorie ON faq_item (categorie);

-- =====================================================================
-- Membres de l'équipe (Comité Central)
-- =====================================================================
CREATE TABLE membre_equipe (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom         TEXT NOT NULL,
    poste       TEXT NOT NULL,
    photo_url   TEXT NULL,
    bio         TEXT NULL,
    ordre       INT NOT NULL DEFAULT 0
);

-- =====================================================================
-- Partenaires (logos et liens)
-- =====================================================================
CREATE TABLE partenaire (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom         TEXT NOT NULL,
    logo_url    TEXT NULL,
    site_web    TEXT NULL,
    ordre       INT NOT NULL DEFAULT 0
);

-- =====================================================================
-- Candidatures publiques (formulaire d'inscription au programme)
-- =====================================================================
CREATE TABLE candidature_publique (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom             TEXT NOT NULL,
    prenom          TEXT NOT NULL,
    email           TEXT NOT NULL,
    telephone       TEXT NULL,
    motivation      TEXT NULL,
    statut          TEXT NOT NULL DEFAULT 'en_attente',
    date_soumission TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_traitement TIMESTAMPTZ NULL,
    commune_id      UUID NULL REFERENCES territoire(id),
    traite_par      UUID NULL REFERENCES utilisateur(id)
);

CREATE INDEX idx_candidature_statut ON candidature_publique (statut);

-- =====================================================================
-- Messages de contact (formulaire public)
-- =====================================================================
CREATE TABLE contact_message (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nom         TEXT NOT NULL,
    email       TEXT NOT NULL,
    sujet       TEXT NOT NULL,
    message     TEXT NOT NULL,
    statut      TEXT NOT NULL DEFAULT 'non_traite',
    date_reception TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_traitement TIMESTAMPTZ NULL
);

CREATE INDEX idx_contact_statut ON contact_message (statut);

-- =====================================================================
-- M1 — Permissions pour la gestion du site public
-- =====================================================================
INSERT INTO permission (code, libelle) VALUES
    ('site_public:read',   'Consulter le contenu du site public'),
    ('site_public:write',  'Créer ou modifier le contenu du site public'),
    ('candidature:read',   'Consulter les candidatures publiques'),
    ('candidature:write',  'Traiter les candidatures publiques'),
    ('contact:read',       'Consulter les messages de contact'),
    ('contact:write',      'Traiter les messages de contact');

-- N1 (Comité Central) reçoit toutes les nouvelles permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r CROSS JOIN permission p
WHERE r.code = 'N1_COMITE_CENTRAL'
  AND p.code IN (
    'site_public:read', 'site_public:write',
    'candidature:read', 'candidature:write',
    'contact:read', 'contact:write'
  );
