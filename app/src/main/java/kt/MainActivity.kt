package kt

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Process.killProcess
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.widget.TextView
import com.snail.update.R
import kt.update.UpdateContract
import kt.update.UpdatePresenter
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), UpdateContract.View {

    private var mDialog: Dialog? = null

    private var mPresenter: UpdatePresenter? = null

    private var mTextView: TextView? = null

    private var version: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTextView = findViewById(R.id.main_textView)

        mPresenter = UpdatePresenter(this)
        mPresenter!!.checkUpdate()
    }

    override fun showDialog() {
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            version = pi.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (mDialog == null)
            mDialog = AlertDialog.Builder(this)
                    .setTitle("检测到有新版本")
                    .setMessage("当前版本:" + version!!)
                    .setPositiveButton("更新") { _, _ -> mPresenter!!.downApk() }
                    .setNegativeButton("忽略") { _, _ -> mPresenter!!.setIgnore(version!!) }
                    .create()

        mDialog!!.show()
    }

    override fun showProgress(size: Int) {
        mTextView!!.text = String.format(Locale.CHINESE, "%d%%", size)
    }

    override fun install(path: String) {
        installApp(path)
    }

    private fun installApp(appFile: String) {
        try {
            val authority = applicationContext.packageName + ".fileProvider"
            val fileUri = FileProvider.getUriForFile(this, authority, File(appFile))
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            } else {
                val uri = Uri.fromFile(File(appFile))
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
            }

            startActivity(intent)

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}