package com.lepu.blepro.observer

/**
 * author: wujuan
 * created on: 2021/1/21 9:23
 * description: 蓝牙状态订阅者
 */
interface BleChangeObserver {

    /**
     * @param curModel 通知来源的设备Model
     * @param state 蓝牙状态 {@link Ble.State}
     */
    fun onBleStateChange(curModel: Int, state: Int)
}