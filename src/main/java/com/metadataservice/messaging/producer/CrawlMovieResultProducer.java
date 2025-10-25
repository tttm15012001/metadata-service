package com.metadataservice.messaging.producer;

import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CrawlMovieResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CrawlMovieResultProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendCrawlResult(Long movieId, CrawlMovieResultMessage message, String responseTopic) {
        log.info("[{}] - [Producer] Sending crawl result to topic '{}'", movieId, responseTopic);
        return Mono.fromFuture(kafkaTemplate.send(responseTopic, message)).then();
    }
}
