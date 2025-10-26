package com.metadataservice.messaging.consumer;

import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_REQUEST;
import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_RESULT;

@Component
@Slf4j
public class CrawlMovieRequestConsumer {

    @Value("${config.crawl.current-year}")
    private Integer currentYear;

    private final MetadataService metadataService;

    @Autowired
    public CrawlMovieRequestConsumer(
            MetadataService metadataService
    ) {
        this.metadataService = metadataService;
    }

    @KafkaListener(topics = TOPIC_MOVIE_CRAWL_REQUEST, groupId = "metadata-service-group")
    public void handleCrawlRequest(Map<String, Object> msg) {
        try {
            String title = (String) msg.get("title");
            Integer releaseYear = msg.get("releaseYear") != null
                    ? (Integer) msg.get("releaseYear")
                    : this.currentYear;
            Number idValue = (Number) msg.get("movieId");
            Long movieId = idValue != null ? idValue.longValue() : null;

            if (movieId == null || title == null) {
                log.warn("[Consumer] missing movie id or title: {}", msg);
                return;
            }

            log.info("[{}] - [Consumer] Received crawl request for {} ({})", movieId, title, releaseYear);

            metadataService.getMetadataByMovieIdOrSearchTitle(movieId, title)
                .ifPresentOrElse(
                    existing -> {
                        log.info("[{}] - Metadata already exists, returning existing data.", movieId);
                        if (!movieId.equals(existing.getMovieId())) {
                            log.info("[{}] - Existing metadata found with different movieId (old={}, new={}) → updating record.",
                                    movieId, existing.getMovieId(), movieId);

                            existing.setMovieId(movieId);
                            metadataService.saveMetadata(existing);
                        }
                        metadataService.returnRetrievedData(movieId, existing, TOPIC_MOVIE_CRAWL_RESULT)
                            .doOnError(err -> log.error("[{}] - Error sending existing metadata: {}", movieId, err.getMessage(), err))
                            .doOnSuccess(v -> log.info("[{}] - Sent existing metadata", movieId))
                            .subscribe();
                    },
                    () -> {
                        log.info("[{}] - Crawling metadata", title);
                        metadataService.crawl(movieId, title, releaseYear, TOPIC_MOVIE_CRAWL_RESULT)
                            .doOnError(err -> log.error("[{}] - Error crawling: {}", movieId, err.getMessage(), err))
                            .doOnSuccess(v -> log.info("[{}] - Saved metadata successfully", movieId))
                            .subscribe();
                    }
                );
        } catch (Exception e) {
            log.error("[Consumer] Unexpected error while handling crawl request: {}", e.getMessage(), e);
        }
    }
}
