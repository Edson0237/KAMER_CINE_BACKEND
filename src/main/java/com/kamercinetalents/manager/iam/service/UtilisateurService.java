package com.kamercinetalents.manager.iam.service;

import com.kamercinetalents.manager.iam.dto.ChangePasswordRequest;
import com.kamercinetalents.manager.iam.dto.CreateUtilisateurRequest;
import com.kamercinetalents.manager.iam.dto.UtilisateurDto;

import java.util.List;
import java.util.UUID;

/**
 * Interface du service de gestion des utilisateurs (module IAM).
 *
 * <p>Définit le contrat métier sans exposer les détails d'implémentation
 * (principe ISP — Interface Segregation Principle). Les contrôleurs
 * dépendent de cette interface, pas de l'implémentation concrète.</p>
 */
public interface UtilisateurService {

    /**
     * Crée un nouvel utilisateur avec hashage du mot de passe.
     *
     * <p>Opération transactionnelle en écriture : l'utilisateur et
     * éventuellement les entités liées sont créés dans la même transaction.</p>
     *
     * @param request les données de création validées
     * @return le DTO de l'utilisateur créé
     */
    UtilisateurDto create(CreateUtilisateurRequest request);

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * <p>Opération de lecture : {@code readOnly = true} au niveau implémentation.</p>
     *
     * @param id l'UUID de l'utilisateur
     * @return le DTO de l'utilisateur
     */
    UtilisateurDto getById(UUID id);

    /**
     * Liste tous les utilisateurs du périmètre de l'utilisateur connecté.
     *
     * <p>Le filtrage territorial est appliqué ici, pas au niveau repository,
     * car il dépend du contexte de sécurité Spring.</p>
     *
     * @return la liste des utilisateurs visibles
     */
    List<UtilisateurDto> listForCurrentTerritoire();

    /**
     * Récupère le profil de l'utilisateur actuellement authentifié.
     *
     * @return le DTO de l'utilisateur connecté
     */
    UtilisateurDto getCurrentUser();

    /**
     * Change le mot de passe de l'utilisateur connecté (volontaire ou forcé
     * après première connexion avec mot de passe temporaire).
     *
     * <p>Vérifie le mot de passe actuel avant d'appliquer le changement.
     * Retire l'indicateur {@code must_change_password} des métadonnées
     * une fois le changement effectué.</p>
     *
     * @param request le mot de passe actuel et le nouveau mot de passe
     */
    void changeOwnPassword(ChangePasswordRequest request);
}
