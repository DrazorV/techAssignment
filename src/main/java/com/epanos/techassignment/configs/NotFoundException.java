package com.epanos.techassignment.configs;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}