package com.metadataservice.service;

import com.metadataservice.model.entity.Metadata;
import reactor.core.publisher.Mono;

public interface MetadataProvider {
    Mono<Metadata> fetch(Long movieId, String title, Integer year);
}

