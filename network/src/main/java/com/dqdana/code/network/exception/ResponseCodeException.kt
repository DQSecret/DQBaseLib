package com.dqdana.code.network.exception

/**
 * 当服务器响应的头不在200与300之间时，说明请求出现了异常，此时应该将此异常主动抛出。
 *
 * @author DQDANA
 * @since 2019/3/11 20:05 PM
 */
class ResponseCodeException(responseCode: Int) :
    RuntimeException("Http request failed with response code $responseCode")