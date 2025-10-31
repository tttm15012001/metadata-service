package com.metadataservice.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.metadataservice.common.converter.CustomDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetadataResponseDto {

    private Long id;

    private Long movieId;

    private Integer tmdbId;

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

    @JsonSerialize(using = CustomDateFormat.class)
    private LocalDate releaseDate;

    private String country;

    private String originalLanguage;

    private List<String> genre;

    private List<ActorResponseDto> actors;

    private String status;
}
