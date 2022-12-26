package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.FscaleUserInfo
import com.lepu.blepro.utils.*

/**
 *
 * 蓝牙操作
 */
class F4ScaleBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "F4ScaleBleInterface"

    private var userInfo = FscaleUserInfo()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = F4ScaleBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = F4ScaleBleManager(context)
            LepuBleLog.d(tag, "!isManagerInitialized, manager.create done")
        }
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: F4ScaleBleResponse.F4ScaleResponse) {
        LepuBleLog.d(tag, "received cmd : " + bytesToHex(response.bytes))
        LepuBleLog.d(tag, "response.unit : " + response.unit)

        when(response.cmd) {
            F4ScaleBleCmd.A0 -> {
                LepuBleLog.d(tag, "model:$model,A0 => success")
                sendCmd(F4ScaleBleCmd.responsePackage(response.packageNo))
            }
            F4ScaleBleCmd.A1 -> {
                LepuBleLog.d(tag, "model:$model,A1 => success")
                sendCmd(F4ScaleBleCmd.responsePackage(response.packageNo))
                var info = F4ScaleBleResponse.BasicData(response.content)
                LepuBleLog.d(tag, "model:$model,BasicData => info.toString() == $info")
            }
            F4ScaleBleCmd.A2 -> {
                LepuBleLog.d(tag, "model:$model,A2 => success")
                var info = F4ScaleBleResponse.StableData(response.content)
                LepuBleLog.d(tag, "model:$model,StableData => info.toString() == $info")
            }
            F4ScaleBleCmd.A3 -> {
                LepuBleLog.d(tag, "model:$model,A3 => success")
                sendCmd(F4ScaleBleCmd.responsePackage(response.packageNo))
                var info = F4ScaleBleResponse.WeightData(response.content)
                LepuBleLog.d(tag, "model:$model,WEIGHT_DATA => info.toString() == $info")
            }
            F4ScaleBleCmd.A4 -> {
                LepuBleLog.d(tag, "model:$model,A4 => success")
                sendCmd(F4ScaleBleCmd.responsePackage(response.packageNo))
                var info = F4ScaleBleResponse.HistoryData(response.content)
                LepuBleLog.d(tag, "model:$model,HistoryData => info.toString() == $info")
            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 4) {
            return bytes
        }
        LepuBleLog.d(tag, "Device Init  " + bytesToHex(bytes))

        loop@ for (i in 0 until bytes.size-3) {

            val len = 16
            if (i+4+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+4+len)

            val crc = (temp.last().toUInt() and 0x1Fu).toInt()

            if (crc == CrcUtil.calF5ScaleCHK(temp)) {
                val bleResponse = F4ScaleBleResponse.F4ScaleResponse(temp)
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+4+len == bytes.size) null else bytes.copyOfRange(i+4+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
    }

    override fun syncTime() {
        sendCmd(F4ScaleBleCmd.updateUserInfo(userInfo.userIndex, userInfo.height, (userInfo.weight*100).toInt(), userInfo.age, userInfo.sex.value))
        LepuBleLog.d(tag, "----------syncTime------------")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }
    /**
     * get real-time data
     */
    override fun getRtData() {
    }

    /**
     * get file list
     */
    override fun getFileList() {
    }

    fun setUserInfo(userInfo: FscaleUserInfo) {
        this.userInfo = userInfo
    }
    fun setUserList(userList: List<FscaleUserInfo>) {
        sendCmd(F4ScaleBleCmd.setUserList(userList))
    }

}