package com.example.emotiongallery.presenter;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.room.Room;

import com.example.emotiongallery.activity.GalleryActivity;
import com.example.emotiongallery.module.Emotion;
import com.example.emotiongallery.module.EmotionDB;
import com.example.emotiongallery.utils.PicUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Presenter {

    private static final String TAG = "Presenter";

    private static final Presenter instance = new Presenter();
    private static volatile EmotionDB emotionDB;
    private static volatile ExecutorService threadPool;

    private Presenter() {
    }

    public static Presenter getInstance() {
        return instance;
    }

    public static EmotionDB getEmotionDB(Context context) {
        if (emotionDB == null) {
            synchronized (EmotionDB.class) {
                if (emotionDB == null) {
                    emotionDB = Room.databaseBuilder(context, EmotionDB.class, "Emotion.db").build();
                }
            }
        }
        return emotionDB;
    }

    public static ExecutorService getThreadPool() {
        if (threadPool == null) {
            threadPool = new ThreadPoolExecutor(
                    32,
                    64,
                    120,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(1024 * 8),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy());
        }
        return threadPool;
    }

    public static List<String> getSortList(Context context) {
        String sortName;
        sortName = context.getSharedPreferences("Data", Context.MODE_PRIVATE).getString("sortList", "默认");
        List<String> list = new ArrayList<>();
        Collections.addAll(list, sortName.split(","));
        return list;
    }

    public static void setSortList(Context context, List<String> sortList) {
        getThreadPool().submit(() -> {
            StringBuilder sb = new StringBuilder();
            for (String sort : sortList) {
                sb.append(sort);
                sb.append(",");
            }
            context.getApplicationContext().getSharedPreferences("Data", Context.MODE_PRIVATE).edit().putString("sortList", sb.toString()).apply();
        });
    }

    //保存表情包到本地
    public void saveEmotions(GalleryActivity activity, List<Uri> uriList, String sort) {
        if (uriList == null || uriList.isEmpty()) return;
        //保存到本地
        for (int i = 0; i < uriList.size(); i++) {
            Uri uri = uriList.get(i);
            //生成id
            Emotion emotion = new Emotion();
            emotion.id = System.currentTimeMillis() + i;
            emotion.sort = sort;
            getThreadPool().submit(() -> {
                try {
                    //获取图片字节
                    byte[] bytes = PicUtils.uriToBytes(activity, uri);
                    //获取文件扩展名
                    String extension = PicUtils.getExtensionByBytes(bytes);
                    Log.i(TAG, "saveEmotions: extension = " + extension);
                    if (extension == null) throw new IOException("unknown extension");
                    //生成文件名
                    emotion.fileName = emotion.id + "." + extension;
                    //生成文件
                    String path = activity.getFilesDir().getAbsolutePath() + "/" + emotion.fileName;
                    File file = new File(path);
                    if (!file.exists()) {
                        if (file.createNewFile()) Log.i(TAG, "create file success");
                        else throw new IOException("create file failed");
                    }
                    //保存到文件
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bytes, 0, bytes.length);
                    fos.flush();
                    fos.close();
                    //保存到数据库
                    EmotionDB db = Presenter.getEmotionDB(activity);
                    db.emotionDao().insert(emotion);
                    Log.i(TAG, "saveEmotion: success");
                    Log.i(TAG, "saveEmotions: path = \n" + path);
                } catch (IOException e) {
                    Log.e(TAG, "saveEmotion", e);
                }
                activity.runOnUiThread(activity::addEmotionFinished);
            });
        }
    }

    //删除表情包
    public void deleteEmotions(GalleryActivity activity, List<Emotion> emotionList) {
        if (emotionList == null || emotionList.isEmpty()) return;
        for (Emotion emotion : emotionList) {
            getThreadPool().submit(() -> {
                //删除文件
                String path = activity.getFilesDir().getAbsolutePath() + "/" + emotion.fileName;
                File file = new File(path);
                if (file.exists()) {
                    if (file.delete()) Log.i(TAG, "delete file success");
                    else Log.e(TAG, "delete file failed", new IOException());
                } else {
                    Log.e(TAG, "deleteEmotions: file not exist");
                    Toast.makeText(activity, "delete file failed!", Toast.LENGTH_SHORT).show();
                }
                //删除数据库中的记录
                EmotionDB db = Presenter.getEmotionDB(activity);
                db.emotionDao().delete(emotion);
                Log.d(TAG, "deleteEmotions: success");
                activity.runOnUiThread(activity::deleteEmotionFinished);
            });
        }
    }

    //删除分类下的所有表情包
    public void deleteEmotionsBySort(GalleryActivity activity, String sort) {
        getThreadPool().submit(() -> getEmotionDB(activity).emotionDao().deleteEmotionsBySort(sort));
    }

    //导出表情包到相册
    public void exportEmotions(GalleryActivity activity, List<Emotion> emotionList) {
        if (emotionList == null || emotionList.isEmpty()) return;
        for (Emotion emotion : emotionList) {
            getThreadPool().submit(() -> {
                try {
                    //有无文件夹，没有就创建
                    File folder = new File(Environment.getExternalStoragePublicDirectory("Pictures").getAbsolutePath() + "/", "EmotionGallery");
                    if (!folder.exists()) {
                        if (folder.mkdirs()) Log.i(TAG, "create folder success");
                        else throw new IOException("create folder failed");
                    }
                    //导出文件
                    String path = activity.getFilesDir().getAbsolutePath() + "/" + emotion.fileName;
                    byte[] bytes = PicUtils.pathToBytes(activity, path);
                    File file = new File(folder, emotion.fileName);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(bytes, 0, bytes.length);
                    fos.flush();
                    fos.close();
                    //相册更新
                    MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, null);
                    Log.i(TAG, "exportEmotions: success");
                    Log.i(TAG, "exportEmotions: path = \n" + file.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "exportEmotions", e);
                }
                activity.runOnUiThread(activity::exportEmotionFinished);
            });
        }
    }
}
