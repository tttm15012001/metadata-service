package com.metadataservice.service;

import reactor.core.publisher.Mono;

public interface MetadataService {
    Mono<Void> crawl(Long movieId, String title, Integer year);
}
