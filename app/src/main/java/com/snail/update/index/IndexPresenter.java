package com.snail.update.index;

import android.os.Environment;
import android.util.Log;

import com.snail.update.utils.DownloadUtils;
import com.snail.update.utils.SpUtils;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

public class IndexPresenter implements IndexContract.Presenter {

    private IndexContract.View view;

    public IndexPresenter(IndexContract.View view) {
        this.view = view;
    }

    /**
     * 请求网络
     * 获取网络版本号
     * 获取成功后与本地版本号比对
     * 符合更新条件就控制view弹窗
     */
    @Override
    public void checkUpdate(String local) {
        //假设获取得到最新版本
        String version = "2.0";
        if (!local.equals(version)) {
            view.showDialog(version);
        }
    }

    /**
     * 设置忽略版本
     */
    @Override
    public void setIgnore(String version) {
        SpUtils.getInstance().putString("ignore",version);
        Log.i("-->","ignore:"+version);
    }

    /**
     * 模拟网络下载
     */
    @Override
    public void downApk() {
//        String url = "http://nosdn-yx.127.net/yxgame/9a52e11533aa469f8f29a38e5ec4f9c0.apk?download=com.netease.hyxd.yixin.163yun.wxkj_1.apk";
        String url = "http://gyxz.exmmw.cn/a3/rj_sp1/niabajiaoyou.apk";
        DownloadUtils.getInstance().download(url,
                Environment.getExternalStorageDirectory().getPath(),
                new DownloadUtils.OnDownloadListener() {
            @Override
            public void downFail(String msg) {
                view.showFail(msg);
            }

            @Override
            public void downSuccess(String path) {
               view.install(path);
            }

            @Override
            public void downProgress(int progress) {
                view.showProgress(progress);
            }
        });
    }
}
