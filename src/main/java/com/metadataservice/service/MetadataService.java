package com.metadataservice.service;

import com.metadataservice.model.entity.Metadata;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface MetadataService {

    Mono<Void> crawl(Long movieId, String title, Integer year, String responseTopic);

    Mono<Void> returnRetrievedData(Long movieId, Metadata metadata, String responseTopic);

    Optional<Metadata> getMetadataByMovieIdOrSearchTitle(Long movieId, String title);

    Metadata saveMetadata(Metadata metadata);
}