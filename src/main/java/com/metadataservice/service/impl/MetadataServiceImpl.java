package com.metadataservice.service.impl;

import com.metadataservice.dto.OmdbSearchResponse;
import com.metadataservice.dto.TmdbDetailSearchResponse;
import com.metadataservice.dto.TmdbSearchResponse;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.repository.MetadataRepository;
import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;


@Service
@Slf4j
public class MetadataServiceImpl implements MetadataService {

    @Value("${config.tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${config.tmdb.token}")
    private String tokenTmDb;

    @Value("${config.omdb.base-url}")
    private String omdbBaseUrl;

    @Value("${config.omdb.token}")
    private String tokenOmDb;

    @Value("${config.tmdb.language}")
    private String language;

    private final MetadataRepository metadataRepository;


    @Autowired
    public MetadataServiceImpl(
        MetadataRepository metadataRepository
    ) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Mono<Void> crawl(String title, Integer year) {
        WebClient tmdbClient = WebClient.builder()
                .baseUrl(tmdbBaseUrl)
                .defaultHeader("Authorization", "Bearer " + tokenTmDb)
                .build();

        WebClient omdbClient = WebClient.builder()
                .baseUrl(omdbBaseUrl)
                .build();

        Mono<TmdbSearchResponse> tmdbSearch = tmdbClient.get()
            .uri(uri -> uri.path("/search/tv")
                    .queryParam("query", title)
                    .queryParam("year", year)
                    .queryParam("language", language)
                    .build())
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("[{}] - TMDb returned error {}:\n{}", title, response.statusCode(), body);
                        return Mono.error(new RuntimeException("[" + title + "]" + "TMDb API error " + response.statusCode()));
                    })
            )
            .bodyToMono(TmdbSearchResponse.class)
            .doOnNext(r -> {
                try {
                    log.info("[{}] - Call TMDb Successfully", title);
                } catch (Exception e) {
                    log.error("[{}] - Failed to log TMDb response", title, e);
                }
            });

        Mono<TmdbDetailSearchResponse> tmdbDetail = tmdbSearch
            .flatMap(search -> {
                var first = search.getResults().stream().findFirst().orElse(null);
                if (first == null) {
                    return Mono.error(new RuntimeException("TMDb not found for: " + title));
                }
                return tmdbClient.get()
                    .uri(uri -> uri.path("/tv/{id}")
                        .build(first.getId()))
                    .retrieve()
                    .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("[{}] - TMDb Detail returned error {}:\n{}", title, response.statusCode(), body);
                                return Mono.error(new RuntimeException("[" + title + "]" + " TMDb Detail API error " + response.statusCode()));
                            })
                    )
                    .bodyToMono(TmdbDetailSearchResponse.class)
                    .doOnNext(r -> {
                        try {
                            log.info("[{}] - Call TMDb Detail Successfully", title);
                        } catch (Exception e) {
                            log.error("[{}] - Failed to log TMDb Detail response", title, e);
                        }
                    });
            });

        Mono<OmdbSearchResponse> omdb = omdbClient.get()
            .uri(uri -> uri.path("/")
                    .queryParam("t", title)
                    .queryParam("apikey", tokenOmDb)
                    .build())
            .retrieve()
            .onStatus(status -> !status.is2xxSuccessful(), response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> {
                        log.error("[{}] - OMDb returned error {}:\n{}", title, response.statusCode(), body);
                        return Mono.error(new RuntimeException("[" + title + "]" + " OMDb API error " + response.statusCode()));
                    })
            )
            .bodyToMono(OmdbSearchResponse.class)
            .doOnNext(r -> {
                try {
                    log.info("[{}] - Call OMDb Successfully", title);
                } catch (Exception e) {
                    log.error("[{}] - Failed to log OMDb response", title, e);
                }
            });

        return Mono.zip(tmdbDetail, omdb)
                .map(tuple -> mergeMetadata(title, tuple.getT1(), tuple.getT2()))
                .flatMap(this::saveOrUpdate)
                .then();
    }

    private Metadata mergeMetadata(String searchTitle, TmdbDetailSearchResponse tmdb, OmdbSearchResponse omdb) {
        if (tmdb == null) throw new RuntimeException("TMDb not found for: " + searchTitle);
        if (omdb == null) throw new RuntimeException("TMDb not found for: " + searchTitle);

        return Metadata.builder()
                .searchTitle(searchTitle)
                .tmdbId(tmdb.getId())
                .forAdult(tmdb.getAdult())
                .title(tmdb.getName())
                .originalTitle(tmdb.getOriginalName())
                .description(tmdb.getOverview())
                .numberOfEpisodes(tmdb.getNumberOfEpisodes())
                .voteAverage(tmdb.getVoteAverage())
                .voteCount(tmdb.getVoteCount())
                .popularity(tmdb.getPopularity())
                .posterPath(omdb.getPoster())
                .backdropPath(tmdb.getBackdropPath())
                .releaseDate(LocalDate.parse(tmdb.getFirstAirDate()))
                .country(omdb.getCountry())
                .originalLanguage(omdb.getLanguage())
                .genre(omdb.getGenre())
                .actors(omdb.getActors())
                .status(tmdb.getStatus())
                .build();
    }

    private Mono<Metadata> saveOrUpdate(Metadata metadata) {
        return Mono.fromCallable(() ->
                metadataRepository.findByTmdbId(metadata.getTmdbId())
                        .map(existing -> {
                            existing.setDescription(metadata.getDescription());
                            existing.setVoteAverage(metadata.getVoteAverage());
                            existing.setPosterPath(metadata.getPosterPath());
                            return metadataRepository.save(existing);
                        })
                        .orElseGet(() -> metadataRepository.save(metadata))
        ).subscribeOn(Schedulers.boundedElastic());
    }
}
