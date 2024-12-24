package com.example.emotiongallery.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
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
import com.example.emotiongallery.presenter.Presenter;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";
    private ActivityResultLauncher<Intent> pickLauncher;
    private Tencent mTencent;

    public TextView manageBtn;
    public TextView deleteBtn;
    public TextView exportBtn;
    public TextView emotionQuantity;
    private TextView exportBtnInPreview;
    private TextView shareBtnInPreview;

    private static final List<Uri> uriList = new ArrayList<>();
    private static final List<Emotion> selectedList = new ArrayList<>();
    private EmotionAdapter adapter;
    private RecyclerView recyclerView;
    private int lastPosition = 0;

    private ConstraintLayout previewLayout;
    private ImageView previewImage;

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
        Tencent.setIsPermissionGranted(true);
        mTencent = Tencent.createInstance("102061317", getApplicationContext(), "com.emotiongallery.provider");
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::dealResult);
        initView();
        initRV();
        initListener();
        overrideBackMethod();
        adapter.refresh(this, "默认");
    }

    //重写回退方法
    private void overrideBackMethod() {
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.getIsMultipleChoice()) {
                    adapter.refresh(GalleryActivity.this, "默认");
                    deleteBtn.setVisibility(View.INVISIBLE);
                    exportBtn.setVisibility(View.INVISIBLE);
                    manageBtn.setVisibility(View.VISIBLE);
                    return;
                }
                finish();
            }
        };
        dispatcher.addCallback(callback);
    }

    private void initView() {
        manageBtn = findViewById(R.id.gallery_bar_manage_button);
        deleteBtn = findViewById(R.id.gallery_bar_delete_button);
        exportBtn = findViewById(R.id.gallery_bar_export_button);
        emotionQuantity = findViewById(R.id.gallery_bar_quantity_number);
        exportBtnInPreview = findViewById(R.id.gallery_preview_export_button);
        shareBtnInPreview = findViewById(R.id.gallery_preview_share_button);
        recyclerView = findViewById(R.id.gallery_emotion_list);
        previewLayout = findViewById(R.id.gallery_preview_layout);
        previewImage = findViewById(R.id.gallery_preview_image);
    }

    private void initRV() {
        adapter = new EmotionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void initListener() {
        manageBtn.setOnClickListener(this::manageBtnClick);
        deleteBtn.setOnClickListener(this::deleteBtnClick);
        exportBtn.setOnClickListener(this::exportBtnClick);
        exportBtnInPreview.setOnClickListener(this::exportBtnClick);
        shareBtnInPreview.setOnClickListener(this::shareBtnClick);
        previewLayout.setOnClickListener(v -> previewLayout.setVisibility(View.INVISIBLE));
    }

    private void manageBtnClick(View view) {
        manageBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        exportBtn.setVisibility(View.VISIBLE);
        adapter.multipleChoice(this);
    }

    private void deleteBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(adapter.getSelectedList());
        Presenter.getInstance().deleteEmotions(this, selectedList);
    }

    private void exportBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(adapter.getSelectedList());
        Presenter.getInstance().exportEmotions(this, adapter.getSelectedList());
    }

    private void shareBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(adapter.getSelectedList());
        final String imagePath = getFilesDir().getAbsolutePath() + "/" + selectedList.get(0).fileName;
        Log.d(TAG, "shareBtnClick: imagePath:\n" + imagePath);
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imagePath);
        runOnUiThread(() -> mTencent.shareToQQ(this, params, QQShareListener));
    }

    IUiListener QQShareListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {

        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onWarning(int i) {

        }
    };

    //打开相册
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickLauncher.launch(intent);
    }

    //处理图片选择结果
    private void dealResult(ActivityResult result) {
        Intent data = result.getData();
        if (data == null) return;
        //将图片uri存入uriList
        uriList.clear();
        if (data.getClipData() != null) {
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++)
                uriList.add(data.getClipData().getItemAt(i).getUri());
        } else if (data.getData() != null) uriList.add(data.getData());
        Log.d(TAG, "onCreate: uriList size: " + uriList.size());
        //保存到内部存储和数据库
        Presenter.getInstance().saveEmotions(this, uriList, "默认");
    }

    //添加表情结束
    public void addEmotionFinished() {
        if (uriList.isEmpty()) return;
        if (++lastPosition != uriList.size()) return;
        lastPosition = 0;
        adapter.refresh(this, "默认");
    }

    //删除表情结束
    public void deleteEmotionFinished() {
        if (selectedList.isEmpty()) return;
        if (++lastPosition != selectedList.size()) return;
        lastPosition = 0;
        adapter.refresh(this, "默认");
    }

    //预览表情
    public void previewEmotion(Emotion emotion) {
        Glide.with(this)
                .load(getFilesDir().getPath() + "/" + emotion.fileName)
                .into(previewImage);
        previewLayout.setVisibility(View.VISIBLE);
    }
}