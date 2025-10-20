package com.metadataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbSearchResponse {
    private Integer page;
    private List<Result> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    @Data
    public static class Result {
        private Integer id;

        private Boolean adult;

        @JsonProperty("backdrop_path")
        private String backdropPath;

        @JsonProperty("genre_ids")
        private List<Integer> genreIds;

        @JsonProperty("origin_country")
        private List<String> originCountry;

        @JsonProperty("original_language")
        private String originalLanguage;

        @JsonProperty("original_name")
        private String originalName;

        @JsonProperty("original_title")
        private String originalTitle;

        private String overview;
        private Double popularity;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("first_air_date")
        private String firstAirDate;

        @JsonProperty("release_date")
        private String releaseDate;

        @JsonProperty("vote_average")
        private Double voteAverage;

        @JsonProperty("vote_count")
        private Integer voteCount;

        // name = for TV, title = for movies
        private String name;

        private String title;
    }
}
