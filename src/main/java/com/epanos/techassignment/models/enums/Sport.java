package com.epanos.techassignment.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Sport {

    FOOTBALL(1),
    BASKETBALL(2);

    private final int code;

    Sport(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static Sport fromCode(int code) {
        return Arrays.stream(values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid sport value: " + code));
    }
}