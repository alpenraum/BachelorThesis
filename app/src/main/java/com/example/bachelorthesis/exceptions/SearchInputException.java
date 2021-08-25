package com.example.bachelorthesis.exceptions;

import java.io.IOException;

/**
 * @author Finn Zimmer
 */
public class SearchInputException extends IOException {
    private final Type type;

    public SearchInputException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        NAME,
        NUMBER
    }
}
