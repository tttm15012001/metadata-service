package com.metadataservice.messaging.consumer;

import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_REQUEST;

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
        String title = (String) msg.get("title");
        Integer releaseYear = msg.get("releaseYear") != null ? (Integer) msg.get("releaseYear") : this.currentYear;
        Number idValue = (Number) msg.get("movieId");
        Long movieId = idValue != null ? idValue.longValue() : null;

        log.info("[Consumer] Crawling metadata for {} ({}) ", title, releaseYear);

        metadataService.crawl(movieId, title, releaseYear)
            .doOnError(err -> log.error("Error crawl {}: {}", title, err.getMessage()))
            .doOnSuccess(v -> log.info("Save metadata successfully for: {}", title))
            .subscribe();
    }
}
