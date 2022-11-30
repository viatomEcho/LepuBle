package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.BiolandBgmBleCmd
import com.lepu.blepro.ble.cmd.BiolandBgmBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.bioland.DeviceInfo
import com.lepu.blepro.ext.bioland.GluData
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 * 血糖仪：
 * send:
 * 1.获取设备信息
 * 2.获取最新一条记录
 * receive:
 * 1.倒计时
 * 2.测量结果
 */
class BiolandBgmBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "BiolandBgmBleInterface"

    private lateinit var context: Context

    private var deviceInfo = DeviceInfo()
    private var gluData = GluData()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = BiolandBgmBleManager(context)
        manager.isUpdater = isUpdater
        manager.setConnectionObserver(this)
        manager.notifyListener = this
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: BiolandBgmBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived received : ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            BiolandBgmBleCmd.HAND_SHAKE -> {
                LepuBleLog.d(tag, "model:$model,HAND_SHAKE => success")
            }
            BiolandBgmBleCmd.GET_INFO -> {
                if (response.content.isEmpty()) return
                val data = BiolandBgmBleResponse.DeviceInfo(response.content)
                LepuBleLog.d(tag, "model:$model,GET_INFO => success $data")

                deviceInfo.version = data.version
                deviceInfo.customerType = data.customerType
                deviceInfo.battery = data.battery
                deviceInfo.deviceType = data.deviceType
                deviceInfo.deviceCode = data.deviceCode
                deviceInfo.sn = data.sn

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmDeviceInfo).post(InterfaceEvent(model, deviceInfo))
            }
            BiolandBgmBleCmd.MSG_ING -> {
                if (response.content.isEmpty()) return
                val data = byte2UInt(response.content[1])
                LepuBleLog.d(tag, "model:$model,MSG_ING => success $data")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmCountDown).post(InterfaceEvent(model, data))
            }
            BiolandBgmBleCmd.GET_DATA -> {
                if (response.content.isEmpty()) return
                val data = BiolandBgmBleResponse.GluData(response.content)
                LepuBleLog.d(tag, "model:$model,GET_DATA => success $data")

                gluData.year = data.year
                gluData.month = data.month
                gluData.day = data.day
                gluData.hour = data.hour
                gluData.minute = data.minute
                gluData.resultMg = data.resultMg
                gluData.resultMmol = data.resultMmol

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmGluData).post(InterfaceEvent(model, gluData))
            }
            BiolandBgmBleCmd.MSG_END -> {
                LepuBleLog.d(tag, "model:$model,MSG_END => success")
                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BiolandBgm.EventBiolandBgmNoGluData).post(InterfaceEvent(model, true))
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 4) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-3) {
            if (bytes[i] != 0x55.toByte()) {
                continue@loop
            }

            // need content length
            val len = byte2UInt(bytes[i+1])

            if (i+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+len)
            if (temp.last() == BiolandBgmBleCmd.getLastByte(temp)) {
                onResponseReceived(BiolandBgmBleResponse.BleResponse(temp))

                val tempBytes: ByteArray? = if (i + len == bytes.size) null else bytes.copyOfRange(i + len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    override fun syncTime() {
//        sendCmd(BiolandBgmBleCmd.setTime())
    }

    override fun getInfo() {
        sendCmd(BiolandBgmBleCmd.getInfo())
        LepuBleLog.e(tag, "getInfo")
    }

    override fun getFileList() {
        sendCmd(BiolandBgmBleCmd.getData())
        LepuBleLog.e(tag, "getFileList")
    }

    override fun dealReadFile(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealReadFile not yet implemented")
    }

    override fun reset() {
        LepuBleLog.e(tag, "reset not yet implemented")
    }

    override fun factoryReset() {
        LepuBleLog.e(tag, "factoryReset not yet implemented")
    }

    override fun factoryResetAll() {
        LepuBleLog.e(tag, "factoryResetAll not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

}