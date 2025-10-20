package com.metadataservice.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Genre {
    ACTION_ADVENTURE(10759, "Action & Adventure"),
    ANIMATION(16, "Animation"),
    COMEDY(35, "Comedy"),
    CRIME(80, "Crime"),
    DOCUMENTARY(99, "Documentary"),
    DRAMA(18, "Drama"),
    FAMILY(10751, "Family"),
    KIDS(10762, "Kids"),
    MYSTERY(9648, "Mystery"),
    NEWS(10763, "News"),
    REALITY(10764, "Reality"),
    SCI_FI_FANTASY(10765, "Sci-Fi & Fantasy"),
    SOAP(10766, "Soap"),
    TALK(10767, "Talk"),
    WAR_POLITICS(10768, "War & Politics"),
    WESTERN(37, "Western");

    private final int id;
    private final String displayName;

    Genre(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public static Genre fromId(int id) {
        return Arrays.stream(values())
                .filter(g -> g.id == id)
                .findFirst()
                .orElse(null);
    }

    public static String getGenreNameFromId(int id) {
        Genre genre = fromId(id);
        return genre != null ? genre.getDisplayName() : "Unknown";
    }
}
