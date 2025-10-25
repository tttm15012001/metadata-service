package com.metadataservice.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, Long id) {
        super(entity + " can't be found with id: " + id);
    }

    public NotFoundException(String entity, String name) {
        super(entity + " can't be found with name: " + name);
    }
}
