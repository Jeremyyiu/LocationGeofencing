package io.locative.app.model;

public interface JsonSerializable<T> {
    String jsonRepresentation();

    T fromJsonRepresentation(String json);
}
