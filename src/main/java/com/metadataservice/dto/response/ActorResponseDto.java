package com.metadataservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorResponseDto {

    private Long id;

    private String name;

    private String profilePath;

    private String characterName;

}
