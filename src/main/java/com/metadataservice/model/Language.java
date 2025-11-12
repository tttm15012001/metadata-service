package com.metadataservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Language {
    KOREAN("ko", "KOREAN"),
    CHINA("zh", "CHINA"),
    US("en", "US");

    private final String code;
    private final String value;

    public static Language fromCode(String code) {
        for(Language lang : Language.values()) {
            if(lang.code.equals(code)) {
                return lang;
            }
        }

        return null;
    }

    public static String getValueFromCode(String code) {
        return Optional.ofNullable(fromCode(code))
                .map(Language::getValue)
                .orElse("Not Found");
    }
}
