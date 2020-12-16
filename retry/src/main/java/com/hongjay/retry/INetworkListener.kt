package com.hongjay.retry

import androidx.annotation.Keep

/**
 * 网络监听
 */
@Keep
interface INetworkListener {
    /**
     * 网络状态
     */
    fun onNetworkState(state: Boolean)
}