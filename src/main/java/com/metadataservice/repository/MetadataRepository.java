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
    Set<String> findAllTitles();

    @Query("SELECT LOWER(m.searchTitle) FROM Metadata m")
    Set<String> findAllSearchTitles();

    Optional<Metadata> findByTmdbId(Integer tmdbId);
}
