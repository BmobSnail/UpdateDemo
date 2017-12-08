package com.snail.update.index;

import android.content.Context;

import java.io.File;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

public interface IndexContract {

    interface View {
        void showUpdate(String version);
        void showProgress(int progress);
        void showFail(String msg);
        void showComplete(File file);
    }

    interface Presenter{
        void checkUpdate(String local);
        void setIgnore(String version);
        void downApk(Context context);
        void unbind(Context context);
    }
}
