package com.cartoffer.web;

import java.util.Map;

public record ApiError(String error, Map<String, Object> details) {
    public static ApiError of(String error, Map<String, Object> details) {
        return new ApiError(error, details);
    }
}
