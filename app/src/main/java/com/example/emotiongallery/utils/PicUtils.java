package com.example.emotiongallery.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PicUtils {
    private static final String TAG = "PicUtils";

    private PicUtils() {
        // This class is not publicly instantiable.
        throw new IllegalStateException();
    }

    @SuppressLint("Recycle")
    public static byte[] uriToByteArray(Context context, Uri uri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while (true) {
            assert is != null;
            if ((len = is.read(buffer)) == -1) break;
            bos.write(buffer, 0, len);
        }
        return bos.toByteArray();
    }
}
