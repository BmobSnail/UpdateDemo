package com.snail.update.index;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

public interface IndexContract {

    interface View {
        void showDialog(String version);
        void showProgress(int size);
        void showFail(String msg);
        void install(String path);
    }

    interface Presenter{
        void checkUpdate(String local);
        void setIgnore(String version);
        void downApk();
    }
}
