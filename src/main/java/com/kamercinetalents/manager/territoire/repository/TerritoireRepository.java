package com.kamercinetalents.manager.territoire.repository;

import com.kamercinetalents.manager.territoire.domain.TerritoireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository Spring Data JPA pour l'entité {@link TerritoireEntity}.
 *
 * <p>Fournit des méthodes de recherche par code, par parent, et par type.
 * Le filtrage par périmètre territorial est appliqué au niveau service
 * car il dépend du contexte de sécurité.</p>
 */
public interface TerritoireRepository extends JpaRepository<TerritoireEntity, UUID> {

    /**
     * Recherche un territoire par son code unique.
     *
     * @param code le code du territoire
     * @return le territoire s'il existe
     */
    Optional<TerritoireEntity> findByCode(String code);

    /**
     * Liste tous les territoires enfants directs d'un parent.
     *
     * @param parentId l'UUID du parent
     * @return la liste des territoires enfants
     */
    List<TerritoireEntity> findByParentId(UUID parentId);

    /**
     * Liste tous les territoires d'un type donné.
     *
     * @param typeTerritoireId l'UUID du type
     * @return la liste des territoires de ce type
     */
    List<TerritoireEntity> findByTypeTerritoireId(UUID typeTerritoireId);
}
