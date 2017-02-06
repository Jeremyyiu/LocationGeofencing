package io.locative.app.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

interface UrlEncoderInterface {
    @Nullable
    String encode(String string);
}

@TargetApi(Build.VERSION_CODES.KITKAT)
class Api19Encoder implements UrlEncoderInterface {
    private static final String ENCODING = "utf-8";

    @Override
    @Nullable
    public String encode(String string) {
        try {
            return URLEncoder.encode(string, ENCODING);
        } catch (Exception ex) {
            return null;
        }
    }
}

class LegacyEncoder implements UrlEncoderInterface {
    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public String encode(String string) {
        return URLEncoder.encode(string);
    }
}

public class UrlEncoder {

    public static String encode(String string) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new Api19Encoder().encode(string);
        }
        return new LegacyEncoder().encode(string);
    }
}
