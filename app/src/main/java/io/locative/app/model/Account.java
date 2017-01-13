package io.locative.app.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

public class Account implements JsonRepresentable<Account> {
    private String mUsername;
    private String mEmail;
    private String mAvatarUrl;

    public Account(@NonNull String username, @NonNull String email, @NonNull String avatarUrl) {
        mUsername = username;
        mEmail = email;
        mAvatarUrl = avatarUrl;
    }

    // todo: figure out if there's a way to use a static method with generics
    // then refactor `fromJsonRepresentation` and get rid of this
    public Account() {

    }

    public String getUsername() {
        return mUsername;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    @Override
    public String jsonRepresentation() {
        return new Gson().toJson(this);
    }

    @Override
    public Account fromJsonRepresentation(String json) {
        return new Gson().fromJson(json, Account.class);
    }
}
