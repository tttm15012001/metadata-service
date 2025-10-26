package com.metadataservice.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtil {
    public static List<String> convertStringToList(String input) {
        if (input == null || input.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static String convertListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        return list.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }

    public static <T> T extractFirstFromList(List<T> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}
