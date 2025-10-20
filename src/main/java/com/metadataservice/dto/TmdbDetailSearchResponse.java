package com.metadataservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TmdbDetailSearchResponse {
    private Integer numberOfEpisodes;
}
