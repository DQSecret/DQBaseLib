package com.dqdana.code.network.request

import android.os.Environment
import com.dqdana.code.core.base.Core
import com.dqdana.code.network.exception.ResponseCodeException
import com.dqdana.code.network.model.Response
import com.dqdana.code.network.util.NetworkConst
import com.dqdana.code.network.util.Utility
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 网络请求模式的基类，所有的请求封装都应该要继承此类。这里会提供网络模块的配置，以及请求的具体逻辑处理等。
 * @author DQDANA
 * @since 2019/3/11 12:42 PM
 */
abstract class Request {

    companion object {
        // 请求的缓存地址
        private const val CACHE_DIR_URI_OK_HTTP = "/yuyinfang/cache"
        private const val CACHE_MAX_SIZE_OK_HTTP = 100L * 1024 * 1024

        // 请求类型
        const val GET = 0
        const val POST = 1
        const val PUT = 2
        const val DELETE = 3
    }

    private lateinit var okHttpClient: OkHttpClient
    private val okHttpBuilder: OkHttpClient.Builder by lazy {
        OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
    }

    private var callback: Callback? = null
    private var getParamsAlready = false
    private var params: Map<String, String>? = null

    init {
        connectTimeout(10)
        writeTimeout(10)
        readTimeout(10)
        cache()
        retry()
    }

    fun connectTimeout(seconds: Int) {
        okHttpBuilder.connectTimeout(seconds.toLong(), TimeUnit.SECONDS)
    }

    fun writeTimeout(seconds: Int) {
        okHttpBuilder.writeTimeout(seconds.toLong(), TimeUnit.SECONDS)
    }

    fun readTimeout(seconds: Int) {
        okHttpBuilder.readTimeout(seconds.toLong(), TimeUnit.SECONDS)
    }

    private fun cache() {
        val cacheDirectory = File(Environment.getExternalStorageDirectory(), CACHE_DIR_URI_OK_HTTP)
        val maxSize = CACHE_MAX_SIZE_OK_HTTP
        val cache = Cache(cacheDirectory, maxSize)
        okHttpBuilder.cache(cache)

    }

    private fun retry() {
        okHttpBuilder.retryOnConnectionFailure(true)
    }

    private fun build() {
        okHttpClient = okHttpBuilder.build()
    }

    /**
     * 设置响应回调接口
     * @param callback：回调的实例
     */
    fun setListener(callback: Callback?) {
        this.callback = callback
    }

    interface Callback {
        fun onResponse(response: Response)
        fun onFailure(e: Exception)
    }

    /**
     * 组装网络请求后添加到HTTP发送队列，并监听响应回调。
     * @param requestModel：网络请求对应的实体类
     */
    fun <T : Response> inFlight(requestModel: Class<T>) {
        // 构造 Client
        build()
        // 构造请求体
        val requestBuilder = okhttp3.Request.Builder()
        // 添加 Header
        requestBuilder.headers(headers(Headers.Builder()).build())
        // 添加 Param
        if (method() == GET && getParams() != null) {
            requestBuilder.url(urlWithParam())
        } else {
            requestBuilder.url(url())
        }
        when {
            method() == POST -> requestBuilder.post(formBody())
            method() == PUT -> requestBuilder.put(formBody())
            method() == DELETE -> requestBuilder.delete(formBody())
        }
        // 发起请求
        okHttpClient.newCall(requestBuilder.build()).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val result = if (body != null) {
                            body.string()
                        } else {
                            ""
                        }
                        val gson = GsonBuilder().disableHtmlEscaping().create()
                        val responseModel = gson.fromJson(result, requestModel)
                        response.close()
                        notifyResponse(responseModel)
                    } else {
                        notifyFailure(ResponseCodeException(response.code()))
                    }
                } catch (e: Exception) {
                    notifyFailure(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                notifyFailure(e)
            }
        })
    }

    abstract fun url(): String // 接口地址
    abstract fun method(): Int // 请求方式 Get Post ...
    abstract fun listener(callback: Callback?)

    /**
     * 获取本次请求所携带的所有参数。
     * @return 本次请求所携带的所有参数，以Map形式返回。
     */
    private fun getParams(): Map<String, String>? {
        if (!getParamsAlready) {
            params = params()
            getParamsAlready = true
        }
        return params
    }

    /**
     * 每个请求拥有不同参数，子类自己设置
     */
    open fun params(): Map<String, String>? {
        return null
    }

    /**
     * Android客户端的所有请求都需要添加User-Agent: GifFun Android这样一个请求头。每个接口的封装子类可以添加自己的请求头。
     * @param builder：请求头builder
     * @return 添加完请求头后的builder。
     */
    open fun headers(builder: Headers.Builder): Headers.Builder {
        builder.add(NetworkConst.HEADER_USER_AGENT, NetworkConst.HEADER_USER_AGENT_VALUE)
        builder.add(NetworkConst.HEADER_APP_VERSION, Utility.appVersion)
        builder.add(NetworkConst.HEADER_APP_SIGN, Utility.appSign)
        return builder
    }

    /**
     * 当GET请求携带参数的时候，将参数以key=value的形式拼装到GET请求URL的后面，并且中间以?符号隔开。
     * @return 携带参数的URL请求地址。
     */
    private fun urlWithParam(): String {
        val params = getParams()
        if (params != null) {
            val keys = params.keys
            if (keys.isNotEmpty()) {
                val paramsBuilder = StringBuilder()
                var needAnd = false
                for (key in keys) {
                    if (needAnd) {
                        paramsBuilder.append("&")
                    }
                    paramsBuilder.append(key).append("=").append(params[key])
                    needAnd = true
                }
                return url() + "?" + paramsBuilder.toString()
            }
        }
        return url()
    }

    /**
     * 构建POST、PUT、DELETE请求的参数体。
     * @return 组装参数后的FormBody。
     */
    private fun formBody(): FormBody {
        val builder = FormBody.Builder()
        val params = getParams()
        if (params != null) {
            val keys = params.keys
            if (!keys.isEmpty()) {
                for (key in keys) {
                    val value = params[key]
                    if (value != null) {
                        builder.add(key, value)
                    }
                }
            }
        }
        return builder.build()
    }

    /**
     * 当请求响应成功的时候，将服务器响应转换后的实体类进行回调。
     * @param response：服务器响应转换后的实体类
     */
    private fun notifyResponse(response: Response) {
        callback?.let {
            if (it is OriginThreadCallback) {
                it.onResponse(response)
                callback = null
            } else {
                Core.handler.post {
                    it.onResponse(response)
                    callback = null
                }
            }
        }
    }

    /**
     * 当请求响应失败的时候，将具体的异常进行回调。
     * @param e：请求响应的异常
     */
    private fun notifyFailure(e: Exception) {
        callback?.let {
            if (it is OriginThreadCallback) {
                it.onFailure(e)
                callback = null
            } else {
                Core.handler.post {
                    it.onFailure(e)
                    callback = null
                }
            }
        }
    }
}