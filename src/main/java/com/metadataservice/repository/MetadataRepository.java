package com.metadataservice.repository;

import com.metadataservice.model.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {

    @Query("SELECT m FROM Metadata m WHERE m.searchTitle = :searchTitle")
    Optional<Metadata> findBySearchTitle(String searchTitle);

    Optional<Metadata> findByMovieIdOrSearchTitle(Long movieId, String searchTitle);

}