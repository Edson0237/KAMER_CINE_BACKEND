package com.kamercinetalents.manager.iam.repository;

import com.kamercinetalents.manager.iam.domain.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité {@link UtilisateurEntity}.
 *
 * <p>Accès en lecture seule aux utilisateurs. Les opérations d'écriture
 * sont gérées par la couche service avec gestion transactionnelle explicite.
 * Le périmètre territorial est filtré au niveau service, pas au niveau
 * repository, car il dépend du contexte de sécurité.</p>
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, UUID> {

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * <p>Utilisé par le processus d'authentification pour retrouver
     * l'utilisateur avant la vérification du mot de passe.</p>
     *
     * @param email l'adresse email à rechercher
     * @return l'utilisateur s'il existe, sinon empty
     */
    Optional<UtilisateurEntity> findByEmail(String email);
}
