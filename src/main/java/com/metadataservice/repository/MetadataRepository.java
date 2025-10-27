package com.metadataservice.repository;

import com.metadataservice.model.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    Optional<Metadata> findByMovieIdOrSearchTitle(Long movieId, String searchTitle);

    Optional<Metadata> findById(Long Id);

    @Query("SELECT m FROM Metadata m LEFT JOIN FETCH m.actors WHERE m.id = :id")
    Optional<Metadata> findByIdWithActors(@Param("id") Long id);

}