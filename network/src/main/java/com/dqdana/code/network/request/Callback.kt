package com.dqdana.code.network.request

import com.dqdana.code.network.model.Response

/**
 * 网络请求响应的回调接口。
 *
 * @author DQDANA
 * @since 2019/3/11 15:21 PM
 */
interface Callback {

    fun onResponse(response: Response)

    fun onFailure(e: Exception)
}