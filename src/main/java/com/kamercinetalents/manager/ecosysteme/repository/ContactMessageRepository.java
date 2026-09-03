package com.kamercinetalents.manager.ecosysteme.repository;

import com.kamercinetalents.manager.ecosysteme.domain.ContactMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessageEntity, UUID> {

    List<ContactMessageEntity> findAllByOrderByDateReceptionDesc();
}
