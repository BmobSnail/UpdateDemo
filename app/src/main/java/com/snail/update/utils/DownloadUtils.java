package com.snail.update.utils;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

public class DownloadUtils {

    private static DownloadUtils mInstance;
    private final OkHttpClient okHttpClient;
    private OnDownloadListener listener;

    public static DownloadUtils getInstance() {
        if (mInstance == null) {
            synchronized (DownloadUtils.class) {
                if (mInstance == null) {
                    mInstance = new DownloadUtils();
                }
            }
        }
        return mInstance;
    }

    private DownloadUtils() {
        okHttpClient = new OkHttpClient();
    }

    public void download(final String url, final String path, OnDownloadListener listener){
        this.listener = listener;
        Request request = new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Message message = Message.obtain();
                message.what = 0;
                message.obj = e.getMessage();
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message message = Message.obtain();
                if (response.body() == null) {
                    message.what = 0;
                    message.obj = "下载错误";
                    handler.sendMessage(message);
                    return;
                }
                InputStream is = null;
                byte[] buff = new byte[2048];
                int len;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = createFile(path);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buff)) != -1) {
                        fos.write(buff,0,len);
                        sum+=len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        message.what = 1;
                        message.obj = progress;
                        handler.sendMessage(message);
                    }
                    fos.flush();
                    message.what = 2;
                    message.obj = file.getAbsoluteFile();
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos != null)
                            fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private File createFile(String path) {
        File file = new File(path,"updateDemo.apk");
        if (!file.exists())
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null ;
    }

    //把处理结果放回ui线程
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    listener.downFail((String) msg.obj);
                    break;

                case 1:
                    listener.downProgress((Integer) msg.obj);
                    break;

                case 2:
                    listener.downSuccess((String) msg.obj);
                    break;
            }
            return false;
        }
    });

    public interface OnDownloadListener{
        void downFail(String msg);
        void downSuccess(String path);
        void downProgress(int progress);
    }

}
