package com.metadataservice.api;

import com.metadataservice.dto.request.CrawlMovieRequest;
import com.metadataservice.dto.response.CrawlMovieResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static com.metadataservice.common.constant.ApiConstants.METADATA_API_URL;

@RequestMapping(METADATA_API_URL)
public interface MetadataApi {

    @PostMapping(value = "/crawl", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CrawlMovieResponse> crawlMovie(@RequestBody List<CrawlMovieRequest> movieRequest);
}
