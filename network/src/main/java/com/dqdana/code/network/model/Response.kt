package com.dqdana.code.network.model

/**
 * 请求响应的基类，这里封装了所有请求都必须会响应的参数，status和msg。
 * @author DQDANA
 * @since 2019/3/11 12:43 PM
 */
open class Response {

    /**
     * 请求结果的状态码
     */
    var status: Int = 0

    /**
     * 请求结果的简单描述。
     */
    var msg: String = ""
}