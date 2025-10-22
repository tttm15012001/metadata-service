package com.metadataservice.messaging.producer;

import com.metadataservice.dto.request.CrawlMovieRequest;
import com.metadataservice.repository.MetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_REQUEST;

@Component
@Slf4j
public class CrawlMovieProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final MetadataRepository metadataRepository;

    public CrawlMovieProducer(
        KafkaTemplate<String, Object> kafkaTemplate,
        MetadataRepository metadataRepository
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.metadataRepository = metadataRepository;
    }

    public void sendCrawlRequest(List<CrawlMovieRequest> crawlMovieRequests) {
        Set<String> existingTitles = metadataRepository.findAllTitles();
        Set<String> existingSearchTitles = metadataRepository.findAllSearchTitles();

        crawlMovieRequests.forEach(request -> {
            String movieTitle = request.getTitle();
            if (!existingTitles.contains(movieTitle.toLowerCase()) && !existingSearchTitles.contains(movieTitle.toLowerCase())) {
                Map<String, Object> payload = Map.of(
                    "title", movieTitle,
                    "originalLanguage", request.getOriginalLanguage(),
                    "requestedAt", Instant.now().toString()
                );

                kafkaTemplate.send(TOPIC_MOVIE_CRAWL_REQUEST, movieTitle, payload);
                log.info("[{}] - Sent crawl request", request.getTitle());
            } else {
                log.info("[{}] - Already have metadata", request.getTitle());
            }
        });
    }
}
