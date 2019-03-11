package com.dqdana.code.core.base

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.support.annotation.CallSuper
import android.support.multidex.MultiDex
import com.dqdana.code.core.BuildConfig

/**
 * 基础的 Application
 * @author DQDANA
 * @since 2019/3/11 2:53 PM
 */
open class BaseApp : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        lateinit var context: Application
            private set
    }

    @CallSuper
    override fun onCreate() {
        setupStrictMode()
        super.onCreate()
        context = this
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            )
        }
        super.onCreate()
    }
}