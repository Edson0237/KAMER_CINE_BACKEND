package com.kamercinetalents.manager.ecosysteme.repository;

import com.kamercinetalents.manager.ecosysteme.domain.FaqItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FaqItemRepository extends JpaRepository<FaqItemEntity, UUID> {

    List<FaqItemEntity> findByActifTrueOrderByCategorieAscOrdreAsc();

    List<FaqItemEntity> findAllByOrderByCategorieAscOrdreAsc();
}
