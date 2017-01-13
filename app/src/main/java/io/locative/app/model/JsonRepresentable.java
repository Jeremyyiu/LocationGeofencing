package io.locative.app.model;

public interface JsonRepresentable<T> {
    String jsonRepresentation();

    T fromJsonRepresentation(String json);
}
