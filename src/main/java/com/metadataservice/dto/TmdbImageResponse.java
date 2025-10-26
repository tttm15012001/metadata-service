package com.metadataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbImageResponse {

    private List<Backdrop> backdrops;

    private List<Logo> logos;

    private List<Poster> posters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Backdrop {

        @JsonProperty("file_path")
        private String filePath;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Logo {

        @JsonProperty("file_path")
        private String filePath;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Poster {

        @JsonProperty("file_path")
        private String filePath;

    }
}
