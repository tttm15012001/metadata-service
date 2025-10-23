package com.metadataservice.messaging.producer;

import com.metadataservice.dto.kafka.CrawlMovieResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.metadataservice.common.constant.KafkaTopicConstants.TOPIC_MOVIE_CRAWL_RESULT;

@Service
@Slf4j
public class CrawlMovieResultProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CrawlMovieResultProducer(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendCrawlResult(CrawlMovieResultMessage message) {
        log.info("CrawlMovieResultProducer.sendCrawlResult");
        return Mono.fromFuture(kafkaTemplate.send(
            TOPIC_MOVIE_CRAWL_RESULT, message
        )).then();
    }
}
