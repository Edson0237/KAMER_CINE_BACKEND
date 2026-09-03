-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration M6 Notifications (socle léger V1)
-- Tables: template_notification, notification, sms_log
-- Mécanisme: bascule SMS pour zones sans data
-- =====================================================================

-- template_notification — modèles de notifications par canal
CREATE TABLE template_notification (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        TEXT NOT NULL UNIQUE,
    canal       TEXT NOT NULL,
    sujet       TEXT NOT NULL,
    corps       TEXT NOT NULL,
    variables   JSONB NULL,
    langue      TEXT NOT NULL DEFAULT 'fr',
    actif       BOOLEAN NOT NULL DEFAULT true
);

-- notification — notification logique adressée à un utilisateur
CREATE TABLE notification (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id     UUID NOT NULL REFERENCES template_notification(id),
    utilisateur_id  UUID NOT NULL REFERENCES utilisateur(id),
    canal           TEXT NOT NULL,
    contenu_final   TEXT NOT NULL,
    statut          TEXT NOT NULL DEFAULT 'en_attente',
    date_envoi      TIMESTAMPTZ NULL,
    date_lecture    TIMESTAMPTZ NULL,
    metadata        JSONB NULL
);

CREATE INDEX idx_notification_utilisateur ON notification (utilisateur_id);
CREATE INDEX idx_notification_statut      ON notification (statut);

-- sms_log — trace dédiée pour les SMS (coût + fournisseur externe)
CREATE TABLE sms_log (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id   UUID NOT NULL REFERENCES notification(id),
    numero_destinataire TEXT NOT NULL,
    fournisseur       TEXT NOT NULL DEFAULT 'orange',
    statut_fournisseur TEXT NULL,
    cout              NUMERIC NULL,
    date_envoi        TIMESTAMPTZ NOT NULL DEFAULT now(),
    tentative         SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_sms_log_notification ON sms_log (notification_id);

-- =====================================================================
-- Seed: templates de notification de base
-- =====================================================================

INSERT INTO template_notification (code, canal, sujet, corps, variables, langue) VALUES
    ('sync.conflit_resolu', 'in_app',
     'Conflit de synchronisation résolu',
     'Une action de synchronisation pour l''entité {{entite_type}} a été résolue automatiquement (Last Write Wins).',
     '{"variables": ["entite_type"]}'::jsonb, 'fr'),

    ('sync.conflit_resolu_sms', 'sms',
     'Conflit de sync',
     'KCT: Conflit sync sur {{entite_type}} resolu automatiquement. Voir l''app au prochain reseau.',
     '{"variables": ["entite_type"]}'::jsonb, 'fr'),

    ('session.cloturee', 'in_app',
     'Session clôturée',
     'La session de formation {{session_lieu}} a été clôturée. Le taux de réussite est de {{taux_reussite}}%.',
     '{"variables": ["session_lieu", "taux_reussite"]}'::jsonb, 'fr'),

    ('session.cloturee_sms', 'sms',
     'Session cloturee',
     'KCT: Session {{session_lieu}} cloturee. Taux de reussite: {{taux_reussite}}%.',
     '{"variables": ["session_lieu", "taux_reussite"]}'::jsonb, 'fr'),

    ('attestation.generee', 'in_app',
     'Attestation générée',
     'Votre attestation pour la session {{session_lieu}} a été générée. Numéro: {{numero}}.',
     '{"variables": ["session_lieu", "numero"]}'::jsonb, 'fr'),

    ('attestation.generee_sms', 'sms',
     'Attestation',
     'KCT: Votre attestation {{numero}} pour la session {{session_lieu}} est disponible.',
     '{"variables": ["session_lieu", "numero"]}'::jsonb, 'fr'),

    ('compte.cree', 'in_app',
     'Compte créé',
     'Votre compte KCT Manager a été créé. Rôle: {{role}}. Territoire: {{territoire}}.',
     '{"variables": ["role", "territoire"]}'::jsonb, 'fr');

-- =====================================================================
-- Seed: paramètres système pour la bascule SMS
-- =====================================================================

INSERT INTO parametre_systeme (cle, valeur, type, description, modifiable_par_role_id)
SELECT 'notification.delai_bascule_sms_secondes', '300', 'integer',
       'Délai en secondes avant bascule in_app vers SMS', r.id
FROM role r WHERE r.code = 'N1_COMITE_CENTRAL'
ON CONFLICT (cle) DO NOTHING;

INSERT INTO parametre_systeme (cle, valeur, type, description, modifiable_par_role_id)
SELECT 'notification.fournisseur_sms_defaut', 'orange', 'string',
       'Fournisseur SMS par défaut (orange/mtn/twilio)', r.id
FROM role r WHERE r.code = 'N1_COMITE_CENTRAL'
ON CONFLICT (cle) DO NOTHING;
