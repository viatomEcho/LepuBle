package com.lepu.blepro.constants

/**
 * author: wujuan
 * created on: 2021/1/22 10:24
 * description:
 */
object Ble {
    /**
     * 蓝牙连接状态
     */
    interface State {
        companion object {
            //不明
            const val UNKNOWN = -1

            //已连接
            const val CONNECTED = 1

            // 未连接
            const val DISCONNECTED = 2

            //连接中
            const val CONNECTING = 3

            //断开中
            const val DISCONNECTING = 4


        }
    }
}