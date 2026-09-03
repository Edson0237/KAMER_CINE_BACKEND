package com.kamercinetalents.manager.ecosysteme.repository;

import com.kamercinetalents.manager.ecosysteme.domain.CandidaturePubliqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidaturePubliqueRepository extends JpaRepository<CandidaturePubliqueEntity, UUID> {

    List<CandidaturePubliqueEntity> findAllByOrderByDateSoumissionDesc();
}
