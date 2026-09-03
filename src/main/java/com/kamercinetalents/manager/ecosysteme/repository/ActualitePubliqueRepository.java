package com.kamercinetalents.manager.ecosysteme.repository;

import com.kamercinetalents.manager.ecosysteme.domain.ActualitePubliqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActualitePubliqueRepository extends JpaRepository<ActualitePubliqueEntity, UUID> {

    List<ActualitePubliqueEntity> findByStatutAndDeletedAtIsNullOrderByDatePublicationDesc(String statut);

    List<ActualitePubliqueEntity> findByDeletedAtIsNullOrderByDatePublicationDesc();
}
