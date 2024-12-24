package com.example.emotiongallery.adapter;

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
    private static final List<Emotion> selectedList = new ArrayList<>();
    private static final Emotion addButton = new Emotion(0, "add_button", null);
    private static boolean isMultipleChoice = false;
    private final Context context;
    private volatile GalleryActivity activity;

    public EmotionAdapter(Context context) {
        this.context = context;
    }

    //刷新列表
    public void refresh(GalleryActivity activity, String sort) {
        this.activity = activity;
        Presenter.getThreadPool().submit(() -> {
            EmotionDB db = Presenter.getEmotionDB(context);
            list.clear();
            list.addAll(db.emotionDao().getEmotionsBySort(sort));
            list.add(addButton);
            Log.d(TAG, "refresh: list.size() = " + list.size());
            isMultipleChoice = false;
            activity.runOnUiThread(this::notifyDataSetChanged);
            activity.runOnUiThread(() -> {
                activity.emotionQuantity.setText(String.valueOf(list.size() - 1));
                activity.manageBtn.setVisibility(View.VISIBLE);
                activity.deleteBtn.setVisibility(View.INVISIBLE);
                activity.exportBtn.setVisibility(View.INVISIBLE);
            });
        });
    }

    //多选模式
    public void multipleChoice(GalleryActivity activity) {
        this.activity = activity;
        selectedList.clear();
        list.remove(addButton);
        Log.d(TAG, "multipleChoice: list.size() = " + list.size());
        isMultipleChoice = true;
        activity.runOnUiThread(this::notifyDataSetChanged);
        activity.emotionQuantity.setText(String.valueOf(selectedList.size()));
    }

    //得到多选状态
    public boolean getIsMultipleChoice() {
        return isMultipleChoice;
    }

    //得到选中的表情
    public List<Emotion> getSelectedList() {
        return selectedList;
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
            //隐藏多余的ImageView
            int index = list.size() - i - 4 * position - 1;
            if (index < 0) {
                holder.emotions.get(i).setVisibility(View.INVISIBLE);
                continue;
            }
            //由于RV的复用机制，所以需要在有图片的区域重新设置显示ImageView
            holder.emotions.get(i).setVisibility(View.VISIBLE);
            //判断是否为多选模式，显示或隐藏蒙版
            if (isMultipleChoice) holder.dividers.get(i).setVisibility(View.VISIBLE);
            else holder.dividers.get(i).setVisibility(View.INVISIBLE);
            //显示添加按钮
            if (index == list.size() - 1 && list.get(index) == addButton) {
                Glide.with(context)
                        .load(R.drawable.ic_add_button)
                        .into(holder.emotions.get(i));
                holder.emotions.get(i).setOnClickListener(v -> activity.runOnUiThread(activity::openGallery));
                continue;
            }

            //显示表情图片
            Glide.with(context)
                    .load(context.getFilesDir().getPath() + "/" + list.get(index).fileName)
                    .into(holder.emotions.get(i));
            holder.emotions.get(i).setOnClickListener(v -> activity.runOnUiThread(() -> {
                //点击表情
                if (isMultipleChoice) {
                    //多选模式下，点击表情切换选择状态
                    int I = list.size() - index - 4 * holder.getAdapterPosition() - 1;
                    if (holder.dividers.get(I).getVisibility() == View.VISIBLE) {
                        holder.dividers.get(I).setVisibility(View.INVISIBLE);
                        selectedList.add(list.get(index));
                    } else {
                        holder.dividers.get(I).setVisibility(View.VISIBLE);
                        selectedList.remove(list.get(index));
                    }
                    activity.emotionQuantity.setText(String.valueOf(selectedList.size()));
                } else {
                    //非多选模式下，点击表情预览
                    selectedList.clear();
                    selectedList.add(list.get(index));
                    activity.previewEmotion(list.get(index));
                }
            }));
        }
    }

    @Override
    public int getItemCount() {
        if (list.size() % 4 == 0) return list.size() / 4;
        else return list.size() / 4 + 1;
    }

    public static class EmotionHolder extends RecyclerView.ViewHolder {

        public List<ImageView> emotions = new ArrayList<>();
        public List<View> dividers = new ArrayList<>();

        public EmotionHolder(@NonNull View itemView) {
            super(itemView);
            emotions.add(itemView.findViewById(R.id.row_emotion_1));
            emotions.add(itemView.findViewById(R.id.row_emotion_2));
            emotions.add(itemView.findViewById(R.id.row_emotion_3));
            emotions.add(itemView.findViewById(R.id.row_emotion_4));
            dividers.add(itemView.findViewById(R.id.row_emotion_divider_1));
            dividers.add(itemView.findViewById(R.id.row_emotion_divider_2));
            dividers.add(itemView.findViewById(R.id.row_emotion_divider_3));
            dividers.add(itemView.findViewById(R.id.row_emotion_divider_4));
        }
    }
}
