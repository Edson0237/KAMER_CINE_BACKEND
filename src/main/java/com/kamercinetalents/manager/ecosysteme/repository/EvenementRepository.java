package com.kamercinetalents.manager.ecosysteme.repository;

import com.kamercinetalents.manager.ecosysteme.domain.EvenementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EvenementRepository extends JpaRepository<EvenementEntity, UUID> {

    List<EvenementEntity> findByDeletedAtIsNullOrderByDateDebutAsc();

    List<EvenementEntity> findByStatutAndDeletedAtIsNullOrderByDateDebutAsc(String statut);
}
