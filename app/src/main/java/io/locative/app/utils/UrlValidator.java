package io.locative.app.utils;

import okhttp3.Request;

/**
 * Created by kida on 13/1/17.
 */

public class UrlValidator {

    private final String url;

    public UrlValidator(String url) {
        this.url = url;
    }

    public boolean validate() {
        try {
            new Request.Builder().url(url).build();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
