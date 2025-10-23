package com.metadataservice.repository;

import com.metadataservice.model.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    @Query("SELECT LOWER(m.title) FROM Metadata m")
    Set<String> findAllSearchTitle();

    @Query("SELECT LOWER(m.searchTitle) FROM Metadata m WHERE m.retrieved = FALSE")
    Set<String> findAllSearchTitlesHasNoMetadata();

    @Query("SELECT m FROM Metadata m WHERE m.searchTitle = :searchTitle")
    Optional<Metadata> findBySearchTitle(String searchTitle);

    Optional<Metadata> findByTmdbId(Integer tmdbId);
}
