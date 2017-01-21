package io.locative.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class ResourceUtils {

    private Context mContext;

    public ResourceUtils(Context context) {
        mContext = context;
    }

    public Uri rawResourceUri(int id) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + id);
    }
}
