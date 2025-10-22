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
public class CrawlMovieConsumer {

    @Value("${config.tmdb.current-year}")
    private Integer currentYear;

    private final MetadataService metadataService;

    @Autowired
    public CrawlMovieConsumer(
        MetadataService metadataService
    ) {
        this.metadataService = metadataService;
    }

    @KafkaListener(topics = TOPIC_MOVIE_CRAWL_REQUEST, groupId = "metadata-crawler-group")
    public void handleCrawlRequest(Map<String, Object> msg) {
        String title = (String) msg.get("title");
        Integer releaseYear = msg.get("releaseYear") != null ? (Integer) msg.get("releaseYear") : this.currentYear;

        System.out.println("[Consumer] Crawling metadata for: " + title + " (" + releaseYear + ")");

        metadataService.crawl(title, releaseYear)
            .doOnError(err -> log.error("Error crawl {}: {}", title, err.getMessage()))
            .doOnSuccess(v -> log.info("Save metadata successfully for: {}", title))
            .subscribe();
    }
}
