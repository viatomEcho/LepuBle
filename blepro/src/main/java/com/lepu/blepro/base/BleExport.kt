package com.lepu.blepro.base

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.lepu.blepro.objs.Bluetooth

/**
 * author: wujuan
 * created on: 2021/1/28 19:21
 * description:
 */
interface BleExport {
    fun reInitBle()

    /**
     * 开始扫描
     *
     */
    fun startScan()

    /**
     * 本次扫描发送配对信息
     */
    fun startScan(p: Boolean)

    /**
     * 开始扫描,组合套装可用此方法来设置扫描条件
     * @param singleScanMode 是否只过滤出targetModel设备 , false时targetModel无效
     * @param targetModel 过滤的设备Model
     *
     */
    fun startScan(singleScanMode: Boolean, targetModel: Int, p: Boolean)

    /**
     * 检查是否有未连接设备，如有开启扫描
     */
    fun hasUnConnected(): Boolean

    fun stopScan()


    fun setInterface(model: Int, isClear: Boolean = true, runRtImmediately: Boolean = false)

    fun connect(context: Context, b: Bluetooth)
    fun connect(context: Context, model: Int, b: BluetoothDevice)


    fun reconnect(model: Int)

    fun reconnect()


    fun disconnect(autoReconnect: Boolean)
    fun disconnect(model: Int, autoReconnect: Boolean)

    fun  getBleState(model: Int): Int


    fun getInfo(model: Int)

    fun readFile(userId: String, fileName: String, model: Int)

    fun stopRtTask(model: Int)

    fun startRtTask(model: Int)

    fun syncData(model: Int, type: String, value: Any)

    fun reset(model: Int)

}