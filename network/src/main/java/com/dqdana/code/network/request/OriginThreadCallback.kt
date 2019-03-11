package com.dqdana.code.network.request

/**
 * 网络请求响应的回调接口，回调时保留原来线程进行回调，不切换到主线程。
 *
 * @author DQDANA
 * @since 2019/3/11 15:21 PM
 */
interface OriginThreadCallback : Callback