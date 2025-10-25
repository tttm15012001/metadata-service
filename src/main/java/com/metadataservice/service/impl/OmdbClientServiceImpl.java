package com.metadataservice.service.impl;

import com.metadataservice.client.ReactiveApiClient;
import com.metadataservice.dto.OmdbSearchResponse;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.service.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for calling OMDb API to fetch movie metadata.
 * Delegates all HTTP operations to ReactiveApiClient.
 */
@Slf4j
@Service
public class OmdbClientServiceImpl implements MetadataProvider {

    private final WebClient client;

    private final ReactiveApiClient apiClient;

    private final String tokenOmdb;

    public OmdbClientServiceImpl(
        WebClient.Builder builder,
        @Value("${config.omdb.base-url}") String baseUrl,
        @Value("${config.omdb.token}") String tokenOmdb,
        ReactiveApiClient apiClient
    ) {
        this.client = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
        this.apiClient = apiClient;
        this.tokenOmdb = tokenOmdb;
    }

    @Override
    public Mono<Metadata> fetch(Long movieId, String title, Integer year) {
        return apiClient.get(
            movieId,
            client,
            "/",
            Map.of(
                "t", title,
                "y", year != null ? year : "",
                "apikey", tokenOmdb
            ),
            OmdbSearchResponse.class,
            "OMDb"
        ).map(response -> {
            log.debug("[{}] - OMDb fetched successfully", movieId);

            return Metadata.builder()
                    .movieId(movieId)
                    .searchTitle(title)
                    .posterPath(response.getPoster())
                    .country(response.getCountry())
                    .originalLanguage(response.getLanguage())
                    .genre(response.getGenre())
                    .actors(response.getActors())
                    .build();
        });
    }
}
