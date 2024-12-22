package com.example.emotiongallery.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotiongallery.R;
import com.example.emotiongallery.activity.GalleryActivity;
import com.example.emotiongallery.module.Emotion;
import com.example.emotiongallery.module.EmotionDB;
import com.example.emotiongallery.presenter.Presenter;

import java.util.ArrayList;
import java.util.List;

public class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.EmotionHolder> {

    private static final String TAG = "EmotionAdapter";

    private static final List<Emotion> list = new ArrayList<>();

    private volatile Context context;
    private volatile GalleryActivity activity;

    public EmotionAdapter(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(GalleryActivity activity, String sort) {
        this.activity = activity;
        new Thread(() -> {
            EmotionDB db = Presenter.getEmotionDB(context);
            list.clear();
            list.addAll(db.emotionDao().getAllEmotions());
            activity.runOnUiThread(this::notifyDataSetChanged);
        }).start();
    }

    @NonNull
    @Override
    public EmotionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_emotion, parent, false);
        return new EmotionHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmotionHolder holder, int position) {
        for (int i = 0; i < 4; i++) {
            int index = list.size() - i - 4 * position - 1;
            if (index < 0) break;
            Glide.with(context)
                    .load(context.getFilesDir().getPath() + "/" + list.get(index).fileName)
                    .into(holder.emotions.get(i));
            holder.emotions.get(i).setOnClickListener(v -> activity.runOnUiThread(() -> activity.previewEmotion(list.get(index))));
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "list.size() = " + list.size());
        if (list.size() % 4 == 0) return list.size() / 4;
        else return list.size() / 4 + 1;
    }

    public static class EmotionHolder extends RecyclerView.ViewHolder {

        public List<ImageView> emotions = new ArrayList<>();

        public EmotionHolder(@NonNull View itemView) {
            super(itemView);
            emotions.add(itemView.findViewById(R.id.row_emotion_1));
            emotions.add(itemView.findViewById(R.id.row_emotion_2));
            emotions.add(itemView.findViewById(R.id.row_emotion_3));
            emotions.add(itemView.findViewById(R.id.row_emotion_4));
        }
    }
}
