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
 *
 * 蓝牙操作
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
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Vtm20fBleResponse.BleResponse) {

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

    /**
     * get device info
     */
    override fun getInfo() {
    }

    override fun syncTime() {

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

    override fun dealReadFile(userId: String, fileName: String) {

    }

    override fun reset() {
    }

    override fun factoryReset() {
    }

    override fun factoryResetAll() {
    }

}