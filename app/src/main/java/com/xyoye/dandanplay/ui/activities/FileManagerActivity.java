package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.DanmuFolderBean;
import com.xyoye.dandanplay.bean.event.OpenDanmuFolderEvent;
import com.xyoye.dandanplay.mvp.impl.DanmuLocalPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DanmuLocalPresenter;
import com.xyoye.dandanplay.mvp.view.DanmuLocalView;
import com.xyoye.dandanplay.ui.weight.SpacesItemDecoration;
import com.xyoye.dandanplay.ui.weight.item.FileManagerItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/2.
 */

public class FileManagerActivity extends BaseMvpActivity<DanmuLocalPresenter> implements DanmuLocalView {
    public final static int DEFAULT_FOLDER = -1;
    public final static int FILE_FOLDER = 0;
    public final static int FILE_DANMU = 1;
    public final static int FILE_SUBTITLE = 2;

    @BindView(R.id.loading_ll)
    LinearLayout loadingLl;
    @BindView(R.id.path_tv)
    TextView pathTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<DanmuFolderBean> adapter;
    private List<DanmuFolderBean> folderBeanList;
    private int fileType;

    @Override
    public void initView() {
        fileType = getFileType();
        folderBeanList = new ArrayList<>();

        if (fileType == FILE_FOLDER || fileType == DEFAULT_FOLDER){
            setTitle("选择文件夹");
        } else if (fileType == FILE_DANMU){
            setTitle("选择本地弹幕");
        } else if (fileType == FILE_SUBTITLE){
            setTitle("选择字幕");
        }

        adapter = new BaseRvAdapter<DanmuFolderBean>(folderBeanList) {
            @NonNull
            @Override
            public AdapterItem<DanmuFolderBean> onCreateItem(int viewType) {
                return new FileManagerItem();
            }
        };
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1,0,0,0));
        recyclerView.setAdapter(adapter);

        presenter.listFile(AppConfig.getInstance().getDownloadFolder());
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected DanmuLocalPresenter initPresenter() {
        return new DanmuLocalPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_file_manager;
    }

    @Override
    public void refreshAdapter(List<DanmuFolderBean> beans) {
        folderBeanList.clear();
        folderBeanList.addAll(beans);
        adapter.notifyDataSetChanged();
    }

    @Override
    public int getFileType(){
        return getIntent().getIntExtra("file_type", 0);
    }

    @Override
    public void showLoading() {
        loadingLl.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingLl.setVisibility(View.GONE);
    }

    @Override
    public void updatePathTitle(String path) {
        pathTv.setText(path);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onpenFolder(OpenDanmuFolderEvent event){
        if (event.isFolder()){
            presenter.listFile(event.getPath());
        }else if (fileType == FILE_DANMU){
            Intent intent = getIntent();
            intent.putExtra("danmu", event.getPath());
            setResult(RESULT_OK, intent);
            finish();
        } else if (fileType == FILE_SUBTITLE){
            Intent intent = getIntent();
            intent.putExtra("subtitle", event.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_video_folder:
                selectedFolder();
                break;
            case R.id.restore_default:
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DanDanPlayer";
                File file = new File(path);
                if (file.exists())
                    file.mkdirs();
                presenter.listFile(path);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (fileType == FILE_FOLDER || fileType == DEFAULT_FOLDER)
            getMenuInflater().inflate(R.menu.menu_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void selectedFolder(){
        String folder = pathTv.getText().toString();
        Intent intent = getIntent();
        intent.putExtra("folder", folder);
        setResult(RESULT_OK, intent);
        finish();
    }
}
