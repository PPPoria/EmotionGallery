package com.example.emotiongallery.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emotiongallery.R;
import com.example.emotiongallery.activity.GalleryActivity;
import com.example.emotiongallery.presenter.Presenter;

import java.util.ArrayList;
import java.util.List;

public class SortAdapter extends RecyclerView.Adapter<SortAdapter.SortHolder> {

    private static final String TAG = "SortAdapter";

    private static final List<String> list = new ArrayList<>();
    private static final String ADD_SORT = "添加分类";
    private final Context context;
    private volatile GalleryActivity activity;

    public SortAdapter(Context context) {
        this.context = context;
    }

    public void refresh(GalleryActivity activity) {
        this.activity = activity;
        Presenter.getThreadPool().submit(() -> {
            Log.i(TAG, "refresh: list size: ");
            list.clear();
            list.addAll(Presenter.getSortList(context));
            list.add(ADD_SORT);
            activity.runOnUiThread(this::notifyDataSetChanged);
        });
    }

    //包含刷新
    public boolean addSort(String sort) {
        for (String s : list) {
            if (s.equals(sort)) return false;
        }
        list.remove(list.size() - 1);
        list.add(sort);
        activity.runOnUiThread(this::notifyDataSetChanged);
        Presenter.setSortList(context, list);
        return true;
    }

    @NonNull
    @Override
    public SortHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_sort, parent, false);
        return new SortHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SortHolder holder, int position) {
        holder.sortName.setText(list.get(position));
        if (position == list.size() - 1) {
            holder.sortName.setTextColor(Color.parseColor("#969696"));
            holder.sortName.setOnClickListener(v -> activity.runOnUiThread(activity::addSort));
        } else {
            holder.sortName.setTextColor(Color.parseColor("#000000"));
            holder.sortName.setOnClickListener(v -> activity.runOnUiThread(() -> activity.changeSort(list.get(position))));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SortHolder extends RecyclerView.ViewHolder {
        public TextView sortName;

        public SortHolder(@NonNull View itemView) {
            super(itemView);
            sortName = itemView.findViewById(R.id.item_sort_name);
        }
    }
}
