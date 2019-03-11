package com.dqdana.code.network.util

import android.os.Build
import android.text.TextUtils
import com.dqdana.code.core.base.Core
import com.dqdana.code.core.extension.logWarn

/**
 * 获取各项基础数据的工具类。
 * @author DQDANA
 * @since 2019/3/11 2:52 PM
 */
object Utility {

    /**
     * 获取设备的品牌和型号，如果无法获取到，则返回Unknown。
     * @return 会以此格式返回数据：品牌 型号。
     */
    val deviceName: String
        get() {
            var deviceName = Build.BRAND + " " + Build.MODEL
            if (TextUtils.isEmpty(deviceName)) {
                deviceName = "unknown"
            }
            return deviceName
        }

    /**
     * 获取当前App的版本号。
     * @return 当前App的版本号。
     */
    val appVersion: String
        get() {
            var version = ""
            try {
                val packInfo = Core.getPackageInfo(0)
                version = packInfo.versionName
            } catch (e: Exception) {
                logWarn("getAppVersion", e.message, e)
            }
            if (TextUtils.isEmpty(version)) {
                version = "unknown"
            }
            return version
        }

    /**
     * 获取App网络请求验证参数，用于辨识是不是官方渠道的App。
     */
    val appSign: String
        get() {
            return MD5.encrypt(SignUtil.getAppSignature() + appVersion)
        }
}