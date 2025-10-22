package com.metadataservice.service;

import reactor.core.publisher.Mono;

public interface MetadataService {
    Mono<Void> crawl(String title, Integer year);
}
