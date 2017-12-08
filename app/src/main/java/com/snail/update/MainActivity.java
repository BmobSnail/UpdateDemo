package com.snail.update;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.snail.update.index.IndexContract;
import com.snail.update.index.IndexPresenter;
import com.snail.update.utils.DownloadService;

import java.io.File;
import java.util.Locale;

import static android.os.Process.killProcess;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo service 配合 okhttp3 下载更新
 */


public class MainActivity extends AppCompatActivity implements IndexContract.View{

    private Dialog mDialog;

    private IndexPresenter mPresenter;

    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.main_textView);

        mPresenter = new IndexPresenter(this);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(),0);
            String local = pi.versionName;
            mPresenter.checkUpdate(local);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showUpdate(final String version) {
        if (mDialog == null)
            mDialog = new AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:"+version)
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.downApk(MainActivity.this);
                        }
                    })
                    .setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mPresenter.setIgnore(version);
                        }
                    })
                    .create();
        mDialog.show();
    }


    @Override
    public void showProgress(int progress) {
        mTextView.setText(String.format(Locale.CHINESE,"%d%%", progress));
    }

    @Override
    public void showComplete(File file) {
        try {
            String authority = getApplicationContext().getPackageName() + ".fileProvider";
            Uri fileUri = FileProvider.getUriForFile(this, authority, file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            } else {
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
            }

            startActivity(intent);

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showFail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        mPresenter.unbind(this);
        super.onDestroy();
    }

}
