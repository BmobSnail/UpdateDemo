package kt.update

import android.os.Handler
import android.util.Log
import com.snail.update.utils.SpUtils

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */

class UpdatePresenter(private val view: UpdateContract.View) : UpdateContract.Presenter {

    //模拟下载进度
    private var size = 0

    /**
     * 下载过程的处理
     */
    private val handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            0 -> view.showProgress(size)

            1 -> {
                view.showProgress(size)
                //下载完毕的完整路径
                view.install("/sdcard/dianfenqi.apk")
            }
        }
        false
    })

    /**
     * 请求网络
     * 获取网络版本号
     * 获取成功后与本地版本号比对
     * 符合更新条件就控制view弹窗
     */
    override fun checkUpdate() {
        view.showDialog()
    }

    /**
     * 设置忽略版本
     */
    override fun setIgnore(version: String) {
        SpUtils.getInstance().putString("ignore", version)
        Log.i("-->", "ignore:" + version)
    }

    /**
     * 模拟网络下载
     */
    override fun downApk() {
        Thread(Runnable {
            while (size < 100) {
                try {
                    Thread.sleep(10)
                    ++size
                    handler.sendEmptyMessage(if (size == 100) 1 else 0)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }).start()
    }
}
