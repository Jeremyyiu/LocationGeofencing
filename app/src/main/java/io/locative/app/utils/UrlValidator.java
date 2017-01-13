package io.locative.app.utils;

import okhttp3.HttpUrl;

public class UrlValidator {

    private final String url;

    public UrlValidator(String url) {
        this.url = url;
    }

    public boolean validate() {
        return HttpUrl.parse(this.url) != null;
    }
}
