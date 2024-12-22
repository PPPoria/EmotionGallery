package com.example.emotiongallery.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.emotiongallery.R;
import com.example.emotiongallery.adapter.EmotionAdapter;
import com.example.emotiongallery.module.Emotion;
import com.example.emotiongallery.overrideview.AnyView;
import com.example.emotiongallery.overrideview.ViewDraw;
import com.example.emotiongallery.presenter.Presenter;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";
    private ActivityResultLauncher<Intent> pickLauncher;

    private AnyView addButton;

    private EmotionAdapter adapter;
    private RecyclerView recyclerView;

    private ConstraintLayout previewLayout;
    private ImageView previewImage;

    private static final List<Uri> uriList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::dealResult);
        initView();
        initAdapter();
        initListener();
        adapter.refresh(this, "默认");
    }

    private void initView() {
        addButton = findViewById(R.id.gallery_add_button);
        addButton.setIconByName(ViewDraw.ADD_BUTTON);
        recyclerView = findViewById(R.id.gallery_emotion_list);
        previewLayout = findViewById(R.id.gallery_preview_layout);
        previewImage = findViewById(R.id.gallery_preview_image);
    }

    private void initAdapter() {
        adapter = new EmotionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initListener() {
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickLauncher.launch(intent);
        });
        previewLayout.setOnClickListener(v -> previewLayout.setVisibility(View.INVISIBLE));
    }

    private void dealResult(ActivityResult result) {
        Intent data = result.getData();
        if (data == null) return;
        //将图片uri存入uriList
        uriList.clear();
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                uriList.add(data.getClipData().getItemAt(i).getUri());
            }
            Log.d(TAG, "onCreate: uriList size: " + uriList.size());
        } else if (data.getData() != null) {
            uriList.add(data.getData());
            Log.d(TAG, "onCreate: uriList size: " + uriList.size());
        }
        //保存到内部存储和数据库
        Presenter.getInstance().saveEmotions(this, uriList, "默认");
    }

    public void previewEmotion(Emotion emotion) {
        Glide.with(this)
                .load(getFilesDir().getPath() + "/" + emotion.fileName)
                .into(previewImage);
        previewLayout.setVisibility(View.VISIBLE);
    }
}