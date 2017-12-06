package com.snail.update;

import android.app.Application;
import android.content.Context;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

public class MyApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
