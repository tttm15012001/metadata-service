package com.metadataservice.controller;

import com.metadataservice.api.MetadataApi;
import com.metadataservice.dto.response.MetadataResponseDto;
import com.metadataservice.model.entity.Metadata;
import com.metadataservice.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class MetadataController implements MetadataApi {

    private final MetadataService metadataService;

    public MetadataController(
            MetadataService metadataService
    ) {
        this.metadataService = metadataService;
    }

    @Override
    public ResponseEntity<MetadataResponseDto> getMetadata(@PathVariable("metadata-id") Long metadataId) {
        MetadataResponseDto metadata = this.metadataService.getMetadataById(metadataId);

        return ResponseEntity.ok(metadata);
    }

}
