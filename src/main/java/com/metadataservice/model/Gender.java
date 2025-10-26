package com.metadataservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum Gender {
    UNKNOWN(0, "Unknown"),
    FEMALE(1, "Female"),
    MALE(2, "Male");

    private final int code;
    private final String label;

    Gender(int code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Gender fromCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return label;
    }
}
