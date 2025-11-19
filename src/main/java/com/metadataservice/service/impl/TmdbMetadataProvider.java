package com.metadataservice.service.impl;

import com.metadataservice.client.ReactiveApiClient;
import com.metadataservice.dto.TmdbAggregateCreditsResponse;
import com.metadataservice.dto.TmdbDetailSearchResponse;
import com.metadataservice.dto.TmdbImageResponse;
import com.metadataservice.dto.TmdbSearchResponse;
import com.metadataservice.model.entity.Actor;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.service.MetadataProvider;
import com.metadataservice.utils.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.metadataservice.common.constant.ApiConstants.SEARCH_TV;
import static com.metadataservice.common.constant.ApiConstants.TV_DETAIL;
import static com.metadataservice.common.constant.ApiConstants.TV_CREDITS;
import static com.metadataservice.common.constant.ApiConstants.TV_IMAGES;

@Service
@Slf4j
public class TmdbMetadataProvider implements MetadataProvider {

    @Value("${config.tmdb.cast-limit}")
    private Integer castLimit;

    @Value("${config.tmdb.image-base-url}")
    private String imageBaseUrl;

    @Value("${config.tmdb.backdrop-size}")
    private String backdropSize;

    @Value("${config.tmdb.poster-size}")
    private String posterSize;

    private final ReactiveApiClient apiClient;

    private final WebClient client;

    private final String language;

    @Autowired
    public TmdbMetadataProvider(
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
        return searchSeries(movieId, title, year)
            .flatMap(first -> fetchEndpoints(movieId, first.getId())
                .map(results -> mapToMetadata(movieId, title, results)))
            .doOnSuccess(meta -> log.debug("[{}] TMDb fetched successfully → {}", movieId, title));
    }

    private Mono<TmdbSearchResponse.Result> searchSeries(Long movieId, String title, Integer year) {
        return apiClient.get(
                movieId, client, SEARCH_TV,
                Map.of("query", title, "year", year, "language", language),
                TmdbSearchResponse.class, "TMDb Master"
        ).map(search -> search.getResults().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("TMDb not found: " + title))
        );
    }

    private Map<String, EndpointSpec<?>> buildEndpoints(Integer tmdbId) {
        return Map.of(
                "general", new EndpointSpec<>(String.format(TV_DETAIL, tmdbId), TmdbDetailSearchResponse.class),
                "aggregate_credits", new EndpointSpec<>(String.format(TV_CREDITS, tmdbId), TmdbAggregateCreditsResponse.class),
                "images", new EndpointSpec<>(String.format(TV_IMAGES, tmdbId), TmdbImageResponse.class)
        );
    }

    private Mono<Map<String, Object>> fetchEndpoints(Long movieId, Integer tmdbId) {
        Map<String, EndpointSpec<?>> endpoints = buildEndpoints(tmdbId);

        return Flux.fromIterable(endpoints.entrySet())
                .flatMap(entry -> apiClient.get(
                        movieId, client,
                        entry.getValue().getPath(),
                        entry.getValue().getResponseType(),
                        entry.getKey()
                ).map(result -> Map.entry(entry.getKey(), result)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    private Metadata mapToMetadata(Long movieId, String title, Map<String, Object> results) {
        var general = (TmdbDetailSearchResponse) results.get("general");
        var images = (TmdbImageResponse) results.get("images");
        var credits = (TmdbAggregateCreditsResponse) results.get("aggregate_credits");

        return Metadata.builder()
                .movieId(movieId)
                .searchTitle(title)
                .tmdbId(general.getId())
                .forAdult(general.getAdult())
                .title(general.getName())
                .originalTitle(general.getOriginalName())
                .description(general.getOverview())
                .numberOfEpisodes(general.getNumberOfEpisodes())
                .voteAverage(general.getVoteAverage())
                .voteCount(general.getVoteCount())
                .popularity(general.getPopularity())
                .posterPath(getPoster(images))
                .backdropPath(getBackdrop(images))
                .releaseDate(general.getFirstAirDate() != null ? LocalDate.parse(general.getFirstAirDate()) : null)
                .country(CommonUtil.extractFirstFromList(general.getOriginCountry()))
                .originalLanguage(general.getOriginalLanguage())
                .genre(general.getGenresAsString())
                .status(general.getStatus())
                .actors(mapActors(credits))
                .build();
    }

    private List<Actor> mapActors(TmdbAggregateCreditsResponse credits) {
        if (credits == null || credits.getCast() == null) return List.of();

        return credits.getCast().stream()
            .limit(castLimit)
            .map(cast -> {
                String character = "";

                if (cast.getRoles() != null && !cast.getRoles().isEmpty()) {
                    character = cast.getRoles().stream()
                        .map(TmdbAggregateCreditsResponse.Cast.Role::getCharacter)
                        .filter(c -> c != null && !c.isBlank())
                        .distinct()
                        .collect(Collectors.joining(", "));
                }

                return Actor.builder()
                    .actorId(cast.getId())
                    .name(cast.getName())
                    .gender(cast.getGender())
                    .characterName(character)
                    .profilePath(cast.getProfilePath())
                    .build();
            })
            .collect(Collectors.toList());
    }

    private String getPoster(TmdbImageResponse images) {
        if (images == null || images.getPosters() == null || images.getPosters().isEmpty()) {
            return null;
        }

        return images.getPosters().get(0).getFilePath();
    }

    private String getBackdrop(TmdbImageResponse images) {
        if (images == null || images.getBackdrops() == null || images.getBackdrops().isEmpty()) {
            return null;
        }

        return images.getBackdrops().get(0).getFilePath();
    }

    @Data
    @AllArgsConstructor
    private static class EndpointSpec<T> {
        private String path;
        private Class<T> responseType;
    }
}
