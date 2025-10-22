package com.metadataservice.controller;

import com.metadataservice.api.MetadataApi;
import com.metadataservice.dto.request.CrawlMovieRequest;
import com.metadataservice.dto.response.CrawlMovieResponse;
import com.metadataservice.messaging.producer.CrawlMovieProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class MetadataController implements MetadataApi {

    private final CrawlMovieProducer crawlMovieProducer;

    @Autowired
    public MetadataController(
            CrawlMovieProducer crawlMovieProducer
    ) {
        this.crawlMovieProducer = crawlMovieProducer;
    }

    @Override
    public ResponseEntity<CrawlMovieResponse> crawlMovie(@RequestBody List<CrawlMovieRequest> movieRequest) {
        log.info("Received {} movies for crawling", movieRequest.size());
        crawlMovieProducer.sendCrawlRequest(movieRequest);

        CrawlMovieResponse response = CrawlMovieResponse.builder()
                .message("Movies are being processed asynchronously")
                .status("FETCHING")
                .requestedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.accepted().body(response);
    }
}
