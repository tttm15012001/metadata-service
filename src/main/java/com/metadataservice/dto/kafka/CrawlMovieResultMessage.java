package com.metadataservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlMovieResultMessage {

    private Long movieId;

    private String title;

    private String backdrop;

    private Long metadataId;

    private String genres;

    private Double voteAverage;

}
