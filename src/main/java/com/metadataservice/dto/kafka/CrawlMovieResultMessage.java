package com.metadataservice.dto.kafka;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CrawlMovieResultMessage {

    private Integer id;

    private Boolean forAdult;

    private String title;

    private String originalTitle;

    private String description;

    private Integer numberOfEpisodes;

    private Double voteAverage;

    private Integer voteCount;

    private Double popularity;

    private String posterPath;

    private String backdropPath;

    private String releaseDate;

    private String country;

    private String originalLanguage;

    private List<Integer> genres;

    private String requestedAt;
}
