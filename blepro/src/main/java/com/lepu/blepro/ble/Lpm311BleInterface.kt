package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.Lpm311Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.HexString.trimStr
import net.litcare.dataparser.lpm311.*
import java.nio.charset.StandardCharsets

/**
 *
 * 蓝牙操作
 */

class Lpm311BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Lpm311BleInterface"

    private lateinit var context: Context

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Lpm311BleManager(context)
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

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: ByteArray) {
        val result = LPMRecordHelper.parseFromBleResult(String(response))
        val data = Lpm311Data()
        data.bytes = response
        data.year = result.year
        data.month = result.month
        data.day = result.day
        data.hour = result.hour
        data.minute = result.min
        data.second = result.sec
        data.chol = result.chol
        data.cholStr = LPMRecordHelper.formatItemText(result, LPMItemType.CHOL)
        data.hdl = result.hdl
        data.hdlStr = LPMRecordHelper.formatItemText(result, LPMItemType.HDL)
        data.trig = result.trig
        data.trigStr = LPMRecordHelper.formatItemText(result, LPMItemType.TRIG)
        data.ldl = result.ldl
        data.ldlStr = LPMRecordHelper.formatItemText(result, LPMItemType.LDL)
        data.cholDivHdl = result.cholDivHdl
        data.cholDivHdlStr = LPMRecordHelper.formatItemText(result, LPMItemType.CHOL_HDL)
        data.unit = when (result.itemUnit) {
            LPMItemUnit.mmol_L -> {
                0
            }
            LPMItemUnit.mg_dL -> {
                1
            }
            else -> 1
        }
        data.user = trimStr(result.name)
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.LPM311.EventLpm311Data).post(InterfaceEvent(model, data))
        sendCmd("disconnect".toByteArray(StandardCharsets.US_ASCII))
        LepuBleLog.d(tag, "onResponseReceived $data")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {

        if (bytes == null || bytes.size < 44) {
            return bytes
        }

        val len = 44

        val temp: ByteArray = bytes.copyOfRange(0, len)
        onResponseReceived(temp)

        val tempBytes: ByteArray? = if (len == bytes.size) null else bytes.copyOfRange(len, bytes.size)

        return hasResponse(tempBytes)

    }

    override fun getFileList() {
        sendCmd("connect".toByteArray(StandardCharsets.US_ASCII))
    }

    override fun syncTime() {
    }

    override fun getInfo() {
    }

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }

    override fun getRtData() {
    }

}