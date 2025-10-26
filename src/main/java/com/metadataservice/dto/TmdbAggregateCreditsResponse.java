package com.metadataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.metadataservice.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmdbAggregateCreditsResponse {

    private List<Cast> cast;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cast {

        private Integer id;

        private String name;

        private Gender gender;

        @JsonProperty("original_name")
        private String originalName;

        @JsonProperty("profile_path")
        private String profilePath;

        private List<Role> roles;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Role {

            private String character;

        }
    }
}
