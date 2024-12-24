package com.example.emotiongallery.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PicUtils {
    private static final String TAG = "PicUtils";

    public static final int UNKNOWN = 0;
    public static final int JPG = 1;
    public static final int PNG = 2;
    public static final int GIF = 3;
    public static final int BMP = 4;
    public static final int WEBP = 5;

    private PicUtils() {
        // This class is not publicly instantiable.
        throw new IllegalStateException();
    }

    @SuppressLint("Recycle")
    public static byte[] uriToBytes(Context context, Uri uri) throws IOException {
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

    public static byte[] pathToBytes(Context context, String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) throw new IOException("File not found: " + path);
        InputStream is = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1)
            bos.write(buffer, 0, len);
        return bos.toByteArray();
    }

    public static int getTypeByBytes(byte[] bytes) {
        int type = UNKNOWN;
        if (bytes.length >= 8) {
            if (bytes[0] == (byte) 0xFF
                    && bytes[1] == (byte) 0xD8)
                type = JPG;
            else if (bytes[0] == (byte) 0x89
                    && bytes[1] == (byte) 0x50
                    && bytes[2] == (byte) 0x4E
                    && bytes[3] == (byte) 0x47
                    && bytes[4] == (byte) 0x0D
                    && bytes[5] == (byte) 0x0A
                    && bytes[6] == (byte) 0x1A
                    && bytes[7] == (byte) 0x0A)
                type = PNG;
            else if (bytes[0] == (byte) 0x47
                    && bytes[1] == (byte) 0x49
                    && bytes[2] == (byte) 0x46
                    && bytes[3] == (byte) 0x38
                    && (bytes[4] == (byte) 0x39 || bytes[4] == (byte) 0x37)
                    && bytes[5] == (byte) 0x61)
                type = GIF;
            else if (bytes[0] == (byte) 0x42
                    && bytes[1] == (byte) 0x4D)
                type = BMP;
            else if (bytes[0] == (byte) 0x57
                    && bytes[1] == (byte) 0x45
                    && bytes[2] == (byte) 0x42
                    && bytes[3] == (byte) 0x50)
                type = WEBP;
        }
        return type;
    }

    public static String getExtensionByBytes(byte[] bytes) {
        int type = getTypeByBytes(bytes);
        switch (type) {
            case JPG:
                return "jpg";
            case PNG:
                return "png";
            case GIF:
                return "gif";
            case BMP:
                return "bmp";
            case WEBP:
                return "webp";
            default:
                return null;
        }
    }
}
