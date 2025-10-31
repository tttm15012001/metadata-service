package com.metadataservice.api;

import com.metadataservice.dto.response.MetadataResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import static com.metadataservice.common.constant.ApiConstants.METADATA_API_URL;

@RequestMapping(METADATA_API_URL)
public interface MetadataApi {

    @GetMapping("/{metadata-id}")
    ResponseEntity<MetadataResponseDto> getMetadata(@PathVariable("metadata-id") Long metadataId);

}
