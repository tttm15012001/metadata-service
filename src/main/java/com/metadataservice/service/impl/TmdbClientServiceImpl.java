package com.metadataservice.service.impl;

import com.metadataservice.client.ReactiveApiClient;
import com.metadataservice.dto.TmdbDetailSearchResponse;
import com.metadataservice.dto.TmdbSearchResponse;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.service.MetadataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

@Service
@Slf4j
public class TmdbClientServiceImpl implements MetadataProvider {
    private final ReactiveApiClient apiClient;
    private final WebClient client;
    private final String language;

    @Autowired
    public TmdbClientServiceImpl(
        WebClient.Builder builder,
        @Value("${config.tmdb.base-url}") String baseUrl,
        @Value("${config.tmdb.token}") String token,
        @Value("${config.tmdb.language}") String language,
        ReactiveApiClient apiClient
    ) {
        this.client = builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
        this.language = language;
        this.apiClient = apiClient;
    }

    @Override
    public Mono<Metadata> fetch(Long movieId, String title, Integer year) {
        return apiClient.get(
            client,
            "/search/tv",
            Map.of(
                "query", title,
                "year", year,
                "language", language
            ),
            TmdbSearchResponse.class,
            "TMDb"
        ).flatMap(search -> {
            var first = search.getResults().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("TMDb not found: " + title));
            return apiClient.get(
                client,
                "/tv/" + first.getId(),
                null,
                TmdbDetailSearchResponse.class,
                "TMDb Detail"
            );
        }).map(response -> {
            log.debug("[{}] - TMDb fetched successfully", title);

            return Metadata.builder()
                    .movieId(movieId)
                    .searchTitle(title)
                    .tmdbId(response.getId())
                    .forAdult(response.getAdult())
                    .title(response.getName())
                    .originalTitle(response.getOriginalName())
                    .description(response.getOverview())
                    .numberOfEpisodes(response.getNumberOfEpisodes())
                    .voteAverage(response.getVoteAverage())
                    .voteCount(response.getVoteCount())
                    .popularity(response.getPopularity())
                    .backdropPath(response.getBackdropPath())
                    .releaseDate(LocalDate.parse(response.getFirstAirDate()))
                    .status(response.getStatus())
                    .build();
        });
    }
}
