package com.example.emotiongallery.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.emotiongallery.adapter.SortAdapter;
import com.example.emotiongallery.module.Emotion;
import com.example.emotiongallery.presenter.Presenter;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";
    private ActivityResultLauncher<Intent> pickLauncher;
    private static final String APP_ID = "102584418";
    private Tencent mTencent;

    //bar
    public TextView sortText;
    public TextView manageBtn;
    public TextView deleteBtn;
    public TextView exportBtn;
    public TextView emotionQuantity;

    //sorts
    public View sortLayout;
    private SortAdapter sortAdapter;
    public RecyclerView sortRV;

    //emotions
    private static final List<Uri> uriList = new ArrayList<>();
    private static final List<Emotion> selectedList = new ArrayList<>();
    private EmotionAdapter emotionsAdapter;
    private RecyclerView emotionsRV;
    private int lastPosition = 0;

    //preview
    private ConstraintLayout previewLayout;
    private ImageView previewImage;
    private TextView exportBtnInPreview;
    private TextView shareBtnInPreview;

    //popup
    private ConstraintLayout popupBackground;
    private ConstraintLayout popupLayout;
    private TextView popupTittle;
    private TextView deleteText;
    private EditText inputText;
    private TextView popupConfirmBtn;
    private TextView popupCancelBtn;
    private static final int POPUP_INPUT = 1;
    private static final int POPUP_DELETE_SORT = 2;
    private int status = 0;

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
        mTencent = Tencent.createInstance(APP_ID, getApplicationContext(), "com.emotiongallery.provider");
        pickLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::dealResult);
        initView();
        initRV();
        initListener();
        overrideBackMethod();
        emotionsAdapter.refresh(this, "默认");
        sortAdapter.refresh(this);
    }

    //重写回退方法
    private void overrideBackMethod() {
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (emotionsAdapter.getIsMultipleChoice()) {
                    //如果处于多选状态，则退出多选状态
                    emotionsAdapter.refresh(GalleryActivity.this, sortText.getText().toString());
                    deleteBtn.setVisibility(View.INVISIBLE);
                    exportBtn.setVisibility(View.INVISIBLE);
                    manageBtn.setVisibility(View.VISIBLE);
                } else if (previewLayout.getVisibility() == View.VISIBLE) {
                    //如果处于预览状态，则退出预览状态
                    previewLayout.setVisibility(View.INVISIBLE);
                } else if (sortLayout.getVisibility() == View.VISIBLE) {
                    //如果处于选择分类状态，则退出选择分类状态
                    sortLayout.setVisibility(View.INVISIBLE);
                } else finish(); //退出程序
            }
        };
        dispatcher.addCallback(callback);
    }

    private void initView() {
        //bar
        sortText = findViewById(R.id.gallery_bar_sort_text);
        manageBtn = findViewById(R.id.gallery_bar_manage_button);
        deleteBtn = findViewById(R.id.gallery_bar_delete_button);
        exportBtn = findViewById(R.id.gallery_bar_export_button);
        emotionQuantity = findViewById(R.id.gallery_bar_quantity_number);
        //preview
        exportBtnInPreview = findViewById(R.id.gallery_preview_export_button);
        shareBtnInPreview = findViewById(R.id.gallery_preview_share_button);
        previewLayout = findViewById(R.id.gallery_preview_layout);
        previewImage = findViewById(R.id.gallery_preview_image);
        //emotionRV
        emotionsRV = findViewById(R.id.gallery_emotion_list);
        //sortRV
        sortLayout = findViewById(R.id.gallery_sort_layout);
        sortRV = findViewById(R.id.gallery_sort_list);
        //popup
        popupBackground = findViewById(R.id.gallery_popup_background);
        popupLayout = findViewById(R.id.gallery_popup_layout);
        popupTittle = findViewById(R.id.gallery_popup_tittle);
        deleteText = findViewById(R.id.gallery_popup_delete_sort_text);
        inputText = findViewById(R.id.gallery_popup_input_sort_edit);
        popupConfirmBtn = findViewById(R.id.gallery_popup_confirm_button);
        popupCancelBtn = findViewById(R.id.gallery_popup_cancel_button);
    }

    private void initRV() {
        emotionsAdapter = new EmotionAdapter(this);
        emotionsRV.setLayoutManager(new LinearLayoutManager(this));
        emotionsRV.setAdapter(emotionsAdapter);
        sortAdapter = new SortAdapter(this);
        sortRV.setLayoutManager(new LinearLayoutManager(this));
        sortRV.setAdapter(sortAdapter);
    }

    private void initListener() {
        manageBtn.setOnClickListener(this::manageBtnClick);
        deleteBtn.setOnClickListener(this::deleteBtnClick);
        exportBtn.setOnClickListener(this::exportBtnClick);
        exportBtnInPreview.setOnClickListener(this::exportBtnClick);
        shareBtnInPreview.setOnClickListener(this::shareBtnClick);
        sortLayout.setOnClickListener(v -> sortLayout.setVisibility(View.INVISIBLE));
        sortText.setOnClickListener(this::sortTextClick);
        previewLayout.setOnClickListener(this::emotionClick);
        popupBackground.setOnClickListener(this::cancelInput);
        popupLayout.setOnClickListener(this::doNothing);
        popupCancelBtn.setOnClickListener(this::cancelInput);
        popupConfirmBtn.setOnClickListener(this::confirmInput);
    }

    private void doNothing(View view) {
        //do nothing
    }

    //表情点击
    private void emotionClick(View view) {
        sortLayout.setVisibility(View.INVISIBLE);
        previewLayout.setVisibility(View.INVISIBLE);
    }

    //管理按钮点击
    private void manageBtnClick(View view) {
        sortLayout.setVisibility(View.INVISIBLE);
        sortText.setVisibility(View.INVISIBLE);
        manageBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        exportBtn.setVisibility(View.VISIBLE);
        emotionsAdapter.multipleChoice(this);
    }

    //删除按钮点击
    private void deleteBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(emotionsAdapter.getSelectedList());
        Presenter.getInstance().deleteEmotions(this, selectedList);
    }

    //导出按钮点击
    private void exportBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(emotionsAdapter.getSelectedList());
        Presenter.getInstance().exportEmotions(this, emotionsAdapter.getSelectedList());
    }

    //分享按钮点击
    private void shareBtnClick(View view) {
        selectedList.clear();
        selectedList.addAll(emotionsAdapter.getSelectedList());
        final String imagePath = getFilesDir().getAbsolutePath() + "/" + selectedList.get(0).fileName;
        Log.d(TAG, "shareBtnClick: imagePath:\n" + imagePath);
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imagePath);
        runOnUiThread(() -> mTencent.shareToQQ(this, params, QQShareListener));
    }

    //被自己幽默到了，没有id分享不了哈哈
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

    //类型点击
    private void sortTextClick(View view) {
        sortAdapter.refresh(this);
        sortLayout.setVisibility(View.VISIBLE);
    }

    //取消按钮点击
    private void cancelInput(View view) {
        popupBackground.setVisibility(View.INVISIBLE);
    }

    //确认按钮点击
    private void confirmInput(View view) {
        if (status == POPUP_INPUT) {
            String sort = inputText.getText().toString();
            if (sort.isEmpty()) return;
            if (sortAdapter.addSort(sort)) {
                emotionsAdapter.refresh(this, sort);
                sortText.setText(sort);
                popupBackground.setVisibility(View.INVISIBLE);
            } else Toast.makeText(this, "分类已存在", Toast.LENGTH_SHORT).show();
        } else if (status == POPUP_DELETE_SORT) {
            String sort = deleteText.getText().toString();
            if (sort.isEmpty()) return;
            if (sortAdapter.deleteSort(sort)) {
                emotionsAdapter.refresh(this, "默认");
                sortText.setText("默认");
                popupBackground.setVisibility(View.INVISIBLE);
                Presenter.getInstance().deleteEmotionsBySort(this, sort);
            } else Toast.makeText(this, "分类不存在", Toast.LENGTH_SHORT).show();
        }
    }

    //分类改变
    public void changeSort(String sort) {
        sortLayout.setVisibility(View.INVISIBLE);
        if (!sort.equals(sortText.getText().toString())) {
            emotionsAdapter.refresh(this, sort);
            sortText.setText(sort);
        }
    }

    //“添加分类”点击
    public void addSort() {
        status = POPUP_INPUT;
        popupBackground.setVisibility(View.VISIBLE);
        popupTittle.setText(getText(R.string.sort_input_hint));
        inputText.setVisibility(View.VISIBLE);
        inputText.setText("");
        deleteText.setVisibility(View.INVISIBLE);
    }

    //“删除分类”点击
    public void deleteSort(String sort) {
        status = POPUP_DELETE_SORT;
        popupBackground.setVisibility(View.VISIBLE);
        popupTittle.setText(getText(R.string.sort_delete_hint));
        inputText.setVisibility(View.INVISIBLE);
        deleteText.setVisibility(View.VISIBLE);
        deleteText.setText(sort);
    }

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
        Presenter.getInstance().saveEmotions(this, uriList, sortText.getText().toString());
    }

    //添加表情结束
    public void addEmotionFinished() {
        if (uriList.isEmpty()) return;
        if (++lastPosition != uriList.size()) return;
        lastPosition = 0;
        emotionsAdapter.refresh(this, sortText.getText().toString());
    }

    //删除表情结束
    public void deleteEmotionFinished() {
        if (selectedList.isEmpty()) return;
        if (++lastPosition != selectedList.size()) return;
        lastPosition = 0;
        emotionsAdapter.refresh(this, sortText.getText().toString());
        Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
    }

    //导出表情结束
    public void exportEmotionFinished() {
        if (selectedList.isEmpty()) return;
        if (++lastPosition != selectedList.size()) return;
        lastPosition = 0;
        emotionsAdapter.refresh(this, sortText.getText().toString());
        Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
    }

    //预览表情
    public void previewEmotion(Emotion emotion) {
        Glide.with(this)
                .load(getFilesDir().getPath() + "/" + emotion.fileName)
                .into(previewImage);
        previewLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        Presenter.getEmotionDB(this).close();
        super.onDestroy();
    }
}