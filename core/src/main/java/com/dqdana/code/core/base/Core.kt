package com.dqdana.code.core.base

import android.content.pm.PackageInfo
import android.os.Handler
import android.os.Looper
import com.dqdana.code.core.BuildConfig

/**
 * 全局的通用组件
 * @author DQDana
 * @since 2019/3/11 7:46 PM
 */
object Core {

    val isDebug by lazy { BuildConfig.DEBUG }

    val handler by lazy { Handler(Looper.getMainLooper()) }

    val BASE_URL by lazy { if (isDebug) "http://192.168.31.177:3000" else "http://api.quxianggif.com" }

    val packageName by lazy { BaseApp.context.packageName }
    val packageManager by lazy { BaseApp.context.packageManager }

    fun getPackageInfo(flags: Int): PackageInfo {
        return packageManager.getPackageInfo(packageName, flags)
    }
}