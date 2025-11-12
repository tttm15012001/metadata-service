package com.metadataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class TmdbDetailSearchResponse {
    private Boolean adult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("genres")
    private List<GenreDto> genres;

    private Integer id;

    private String name;

    @JsonProperty("original_name")
    private String originalName;

    private String overview;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    @JsonProperty("original_language")
    private String originalLanguage;

    private Double popularity;

    private String status;

    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreDto {

        private int id;

        private String name;

    }

    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) return null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(genres);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert genres to JSON", e);
        }
    }
}
