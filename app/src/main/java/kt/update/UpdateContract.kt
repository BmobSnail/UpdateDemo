package kt.update

/**
 * Created by snail
 * on 2017/12/6.
 * Todo
 */
interface UpdateContract{

    interface View {
        fun showDialog()
        fun showProgress(size: Int)
        fun install(path: String)
    }

    interface Presenter {
        fun checkUpdate()
        fun setIgnore(version: String)
        fun downApk()
    }
}