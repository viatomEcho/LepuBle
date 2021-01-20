package com.lepu.blepro.observer.O2

import com.lepu.blepro.ble.BleServiceHelper
import com.lepu.blepro.constants.BleConst

/**
 * author: wujuan
 * created on: 2020/12/17 19:52
 * description: 供外部使用的蓝牙状态改变的订阅者超类
 *              使用：实现该接口，覆盖一下方法
 */
interface O2BleObserver {
    /**
     * 当OxyBleInterface 蓝牙状态改变
     */
    fun onBleStateChange(state: Int)


}