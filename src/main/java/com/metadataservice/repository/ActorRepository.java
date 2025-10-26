package com.metadataservice.repository;

import com.metadataservice.model.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    Optional<Actor> findByActorId(Integer actorId);
}
