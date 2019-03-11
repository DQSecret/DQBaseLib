import com.qiniu.android.http.ResponseInfo
import com.qiniu.android.storage.UpCancellationSignal
import com.qiniu.android.storage.UpProgressHandler
import com.qiniu.android.storage.UploadManager
import com.qiniu.android.storage.UploadOptions

/**
 * 用于进行七牛云上传操作的管理类
 * @author DQDANA
 * @since 2019/3/11 12:38 PM
 */
object QiNiuManager {

    private var uploadManager: UploadManager = UploadManager()

    private var isCanceled: Boolean = false

    fun upload(filePath: String, key: String, upToken: String, listener: UploadListener?) {
        uploadManager.put(filePath, key, upToken, { apiKey, info, _ ->
            if (listener != null && info != null) {
                if (info.isOK) {
                    listener.onSuccess(apiKey)
                } else {
                    listener.onFailure(info)
                }
            }
        }, UploadOptions(null, null, false, UpProgressHandler { _, percent ->
            listener?.onProgress(percent)
        }, UpCancellationSignal { isCanceled }))
    }


    fun cancel() {
        isCanceled = true
    }

    interface UploadListener {
        fun onSuccess(key: String)
        fun onFailure(info: ResponseInfo?)
        fun onProgress(percent: Double)
    }
}