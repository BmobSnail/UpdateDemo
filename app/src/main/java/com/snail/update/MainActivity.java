package com.snail.update;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.snail.update.index.IndexContract;
import com.snail.update.index.IndexPresenter;
import com.snail.update.utils.SpUtils;

import java.io.File;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.os.Process.killProcess;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo service 配合 okhttp3 下载更新
 */



@RuntimePermissions
public class MainActivity extends AppCompatActivity implements IndexContract.View{

    private Dialog mDialog;

    private IndexPresenter mPresenter;

    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPresenter = new IndexPresenter(this);
        mTextView = findViewById(R.id.main_textView);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextView.getText().toString().equals("下载进度")) {
                    SpUtils.getInstance().putString("ignore","-1");
                    MainActivityPermissionsDispatcher.needStorageWithPermissionCheck(MainActivity.this);
                }
            }
        });
        MainActivityPermissionsDispatcher.needStorageWithPermissionCheck(this);
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

        //重写这俩个方法，一般是强制更新不能取消弹窗
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK && mDialog != null && mDialog.isShowing();
            }
        });

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

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void needStorage() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(),0);
            String local = pi.versionName;
            mPresenter.checkUpdate(local);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                needStorage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRational(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("温馨提示")
                .setMessage("赋予此应用读写文件权限用于版本更新，是否同意?")
                .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void deniedStorage() {
        Toast.makeText(this, "我已经拒绝过你了，别来了", Toast.LENGTH_SHORT).show();
    }
}
