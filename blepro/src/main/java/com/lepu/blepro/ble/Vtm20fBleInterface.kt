package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.ext.vtm20f.*
import com.lepu.blepro.utils.*
import com.lepu.blepro.utils.ByteUtils.byte2UInt

/**
 * vtm20f指甲血氧：
 * receive:
 * 1.实时血氧
 * 血氧采样率：参数1HZ，波形50HZ
 */

class Vtm20fBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Vtm20fBleInterface"

    private lateinit var context: Context

    private var param = RtParam()
    private var wave = RtWave()

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        this.context = context
        manager = Vtm20fBleManager(context)
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
    private fun onResponseReceived(response: Vtm20fBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived bytes: ${bytesToHex(response.bytes)}")
        when (response.cmd) {
            Vtm20fBleResponse.RT_PARAM -> {
                LepuBleLog.d(tag, "model:$model,RT_PARAM => success " + bytesToHex(response.bytes))
                val info = Vtm20fBleResponse.RtParam(response.content)

                param.seqNo = info.seqNo
                param.spo2 = info.spo2
                param.pr = info.pr
                param.pi = info.pi

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtParam).post(InterfaceEvent(model, param))
            }
            Vtm20fBleResponse.RT_WAVE -> {
                LepuBleLog.d(tag, "model:$model,RT_WAVE => success " + bytesToHex(response.bytes))
                val info = Vtm20fBleResponse.RtWave(response.content)

                wave.seqNo = info.seqNo
                wave.wave = info.wave
                wave.barChart = info.barChart
                wave.isPulseSound = info.pulseSound
                wave.isDisturb = info.isDisturb
                wave.isSensorOff = info.isSensorOff
                wave.isLowPi = info.isLowPi

                LiveEventBus.get<InterfaceEvent>(InterfaceEvent.VTM20f.EventVTM20fRtWave).post(InterfaceEvent(model, wave))
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
            if (bytes[i] != 0xFE.toByte()) {
                continue@loop
            }

            // need content length
            val len = byte2UInt(bytes[i+1])
            if (i+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+len)
            val bleResponse = Vtm20fBleResponse.BleResponse(temp)
            onResponseReceived(bleResponse)

            val tempBytes: ByteArray? = if (i+len == bytes.size) null else bytes.copyOfRange(i+len, bytes.size)

            return hasResponse(tempBytes)
        }

        return bytesLeft
    }

    override fun getInfo() {
        LepuBleLog.e(tag, "getInfo not yet implemented")
    }

    override fun syncTime() {
        LepuBleLog.e(tag, "syncTime not yet implemented")
    }

    override fun dealContinueRF(userId: String, fileName: String) {
        LepuBleLog.e(tag, "dealContinueRF not yet implemented")
    }

    override fun getRtData() {
        LepuBleLog.e(tag, "getRtData not yet implemented")
    }

    override fun getFileList() {
        LepuBleLog.e(tag, "getFileList not yet implemented")
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

}