-- =====================================================================
-- KAMER CINÉ TALENTS MANAGER — Migration V6: seed paramètre seuil réussite
-- =====================================================================

INSERT INTO parametre_systeme (cle, valeur, type, description, modifiable_par_role_id)
SELECT 'formation.seuil_reussite', '10', 'integer',
       'Seuil de note (sur 20) pour considérer un apprenant comme ayant réussi',
       r.id
FROM role r WHERE r.code = 'N1_COMITE_CENTRAL'
ON CONFLICT (cle) DO NOTHING;
