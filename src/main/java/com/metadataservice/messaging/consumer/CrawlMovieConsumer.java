package com.metadataservice.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.metadataservice.dto.TmdbDetailSearchResponse;
import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import com.metadataservice.dto.TmdbSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_RESULT;
import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_REQUEST;

@Component
public class CrawlMovieConsumer {

    @Value("${config.tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${config.tmdb.bearer}")
    private String bearerToken;

    @Value("${config.tmdb.current-year}")
    private String currentYear;

    @Value("${config.tmdb.language}")
    private String language;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public CrawlMovieConsumer(
        KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = TOPIC_MOVIE_CRAWL_REQUEST, groupId = "metadata-service")
    public void handleCrawlRequest(Map<String, Object> msg) {
        String title = (String) msg.get("title");
        String releaseYear = msg.get("releaseYear") != null ? (String) msg.get("releaseYear") : this.currentYear;

        System.out.println("🎥 [Consumer] Crawling metadata for: " + title + " (" + releaseYear + ")");

        CompletableFuture.runAsync(() -> {
            try {
                CrawlMovieResultMessage metadata = fetchFromTmdb(title, releaseYear);
                metadata.setRequestedAt((String) msg.get("requestedAt"));

                kafkaTemplate.send(TOPIC_MOVIE_CRAWL_RESULT, metadata);
                System.out.println("[Producer] Sent metadata result for: " + title);
            } catch (Exception e) {
                System.err.println("[Consumer] Failed TMDb fetch for " + title + ": " + e.getMessage());
            }
        });
    }

    private CrawlMovieResultMessage fetchFromTmdb(String title, String currentYear) {
        WebClient client = WebClient.builder()
                .baseUrl(tmdbBaseUrl)
                .defaultHeader("Authorization", "Bearer " + bearerToken)
                .build();

        TmdbSearchResponse search = client.get()
                .uri(uri -> uri.path("/search/tv")
                        .queryParam("query", title)
                        .queryParam("year", currentYear)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .block();

        if (search == null || search.getResults().isEmpty()) {
            throw new RuntimeException("Not found on TMDb: " + title);
        }

        var first = search.getResults().get(0);
        Integer tmdbId = first.getId();

        TmdbDetailSearchResponse detail = client.get()
                .uri(uri -> uri.path("/tv/" + tmdbId)
                        .queryParam("language", language)
                        .build())
                .retrieve()
                .bodyToMono(TmdbDetailSearchResponse.class)
                .block();

        return CrawlMovieResultMessage.builder()
                .id(first.getId())
                .title(first.getName() != null ? first.getName() : first.getTitle())
                .originalTitle(first.getOriginalName() != null ? first.getOriginalName() : first.getOriginalTitle())
                .description(first.getOverview())
                .numberOfEpisodes(detail != null ? detail.getNumberOfEpisodes() : 0)
                .voteAverage(first.getVoteAverage())
                .voteCount(first.getVoteCount())
                .popularity(first.getPopularity())
                .posterPath(first.getPosterPath())
                .backdropPath(first.getBackdropPath())
                .releaseDate(first.getFirstAirDate() != null ? first.getFirstAirDate() : first.getReleaseDate())
                .country(first.getOriginCountry().get(0))
                .originalLanguage(first.getOriginalLanguage())
                .genres(first.getGenreIds())
                .build();
    }
}
