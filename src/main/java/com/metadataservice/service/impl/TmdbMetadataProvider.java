package com.metadataservice.service.impl;

import com.metadataservice.client.ReactiveApiClient;
import com.metadataservice.dto.TmdbAggregateCreditsResponse;
import com.metadataservice.dto.TmdbDetailSearchResponse;
import com.metadataservice.dto.TmdbImageResponse;
import com.metadataservice.dto.TmdbSearchResponse;
import com.metadataservice.model.entity.Actor;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.repository.ActorRepository;
import com.metadataservice.service.MetadataProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TmdbMetadataProvider implements MetadataProvider {

    @Value("${config.tmdb.cast-limit}")
    private Integer castLimit;

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
        return apiClient.get(
                movieId,
                client,
                "/search/tv",
                Map.of(
                        "query", title,
                        "year", year,
                        "language", language
                ),
                TmdbSearchResponse.class,
                "TMDb Master"
        ).flatMap(search -> {
            var first = search.getResults().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("TMDb not found: " + title));

            Integer tmdbId = first.getId();

            Map<String, EndpointSpec<?>> endpoints = Map.of(
                    "general", new EndpointSpec<>("/tv" + tmdbId, TmdbDetailSearchResponse.class),
                    "aggregate_credits", new EndpointSpec<>("/tv/" + tmdbId + "/aggregate_credits", TmdbAggregateCreditsResponse.class),
                    "images", new EndpointSpec<>("/tv/" + tmdbId + "/images", TmdbImageResponse.class)
            );

            Mono<Map<String, Object>> resultsMono = Flux.fromIterable(endpoints.entrySet())
                    .flatMap(entry -> apiClient.get(
                            movieId,
                            client,
                            entry.getValue().getPath(),
                            entry.getValue().getResponseType(),
                            entry.getKey()
                    ).map(result -> Map.entry(entry.getKey(), result)))
                    .collectMap(Map.Entry::getKey, Map.Entry::getValue);

            return resultsMono.map(results -> {
                var detail = (TmdbDetailSearchResponse) results.get("detail");
                var images = (TmdbImageResponse) results.get("images");
                var credits = (TmdbAggregateCreditsResponse) results.get("aggregate_credits");

                List<Actor> actors = mapActors(credits);

                String posterPath = getPoster(images);
                String backdropPath = getBackdrop(images);

                return Metadata.builder()
                        .movieId(movieId)
                        .searchTitle(title)
                        .tmdbId(detail.getId())
                        .forAdult(detail.getAdult())
                        .title(detail.getName())
                        .originalTitle(detail.getOriginalName())
                        .description(detail.getOverview())
                        .numberOfEpisodes(detail.getNumberOfEpisodes())
                        .voteAverage(detail.getVoteAverage())
                        .voteCount(detail.getVoteCount())
                        .popularity(detail.getPopularity())
                        .posterPath(posterPath)
                        .backdropPath(backdropPath)
                        .releaseDate(detail.getFirstAirDate() != null ? LocalDate.parse(detail.getFirstAirDate()) : null)
                        .country(detail.getOriginCountry().get(0))
                        .originalLanguage(detail.getOriginalLanguage())
                        .genre(detail.getGenresAsString())
                        .status(detail.getStatus())
                        .actors(actors)
                        .build();
            });
        }).doOnSuccess(v ->
                log.debug("[{}] TMDb fetched successfully → {}", movieId, title)
        );
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
                            .character(character)
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

    // Generic endpoint holder
    @Data
    @AllArgsConstructor
    private static class EndpointSpec<T> {
        private String path;
        private Class<T> responseType;
    }
}
