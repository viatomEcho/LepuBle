package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.data.Lpm311Data
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

/**
 *
 * 蓝牙操作
 */

class Lpm311BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Lpm311BleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        manager = Lpm311BleManager(context)
        manager?.let {
            it.isUpdater = isUpdater
            it.setConnectionObserver(this)
            it.notifyListener = this
            it.connect(device)
                .useAutoConnect(false)
                .timeout(10000)
                .retry(3, 100)
                .done {
                    LepuBleLog.d(tag, "manager.connect done")
                }
                .enqueue()
        } ?: kotlin.run {
            LepuBleLog.d(tag, "manager == null")
        }
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(bytes: ByteArray) {
        var index = 0
        val data = Lpm311Data()
        data.bytes = bytes
        data.year = if (isNumber(String(bytes.copyOfRange(index, index+4)))) {
            String(bytes.copyOfRange(index, index+4)).toInt()
        } else {
            0
        }
        index += 4
        data.month = if (isNumber(String(bytes.copyOfRange(index, index+2)))) {
            String(bytes.copyOfRange(index, index+2)).toInt()
        } else {
            0
        }
        index += 2
        data.day = if (isNumber(String(bytes.copyOfRange(index, index+2)))) {
            String(bytes.copyOfRange(index, index+2)).toInt()
        } else {
            0
        }
        index += 2
        data.hour = if (isNumber(String(bytes.copyOfRange(index, index+2)))) {
            String(bytes.copyOfRange(index, index+2)).toInt()
        } else {
            0
        }
        index += 2
        data.minute = if (isNumber(String(bytes.copyOfRange(index, index+2)))) {
            String(bytes.copyOfRange(index, index+2)).toInt()
        } else {
            0
        }
        index += 2
        data.second = if (isNumber(String(bytes.copyOfRange(index, index+2)))) {
            String(bytes.copyOfRange(index, index+2)).toInt()
        } else {
            0
        }
        index += 2
        index++
        data.chol = if (String(bytes.copyOfRange(index, index + 8))[0] == 'f') {
            0.0
        } else {
            toIntBig(HexString.hexToBytes(String(bytes.copyOfRange(index, index + 8)))).toDouble()
        }
        index += 8
        data.hdl = if (String(bytes.copyOfRange(index, index + 8))[0] == 'f') {
            0.0
        } else {
            toIntBig(HexString.hexToBytes(String(bytes.copyOfRange(index, index + 8)))).toDouble()
        }
        index += 8
        data.trig = if (String(bytes.copyOfRange(index, index + 8))[0] == 'f') {
            0.0
        } else {
            toIntBig(HexString.hexToBytes(String(bytes.copyOfRange(index, index + 8)))).toDouble()
        }
        index += 8
        index++
        data.unit = if (isNumber(String(bytes.copyOfRange(index, index+1)))) {
            String(bytes.copyOfRange(index, index+1)).toInt()
        } else {
            0
        }
        index++
        data.user = HexString.trimStr(String(bytes.copyOfRange(index, bytes.size)))
        when (data.unit) {
            Lpm311Data.UNIT_MMOL -> {
                data.chol = data.chol.div(100)
                data.hdl = data.hdl.div(100)
                data.trig = data.trig.div(100)
                data.ldl = if (data.chol == 0.0 || data.hdl == 0.0 || data.trig == 0.0) {
                    0.0
                } else {
                    data.chol - data.hdl - data.trig.div(2.2)
                }
                data.cholDivHdl = if (data.chol == 0.0 || data.hdl == 0.0) {
                    0.0
                } else if (data.chol < Lpm311Data.CHOL_MMOL_MIN || data.chol > Lpm311Data.CHOL_MMOL_MAX) {
                    0.0
                } else if (data.hdl < Lpm311Data.HDL_MMOL_MIN || data.hdl > Lpm311Data.HDL_MMOL_MAX) {
                    0.0
                } else {
                    data.chol.div(data.hdl)
                }
            }
            Lpm311Data.UNIT_MG -> {
                data.ldl = if (data.chol == 0.0 || data.hdl == 0.0 || data.trig == 0.0) {
                    0.0
                } else {
                    data.chol - data.hdl - data.trig.div(5)
                }
                data.cholDivHdl = if (data.chol == 0.0 || data.hdl == 0.0) {
                    0.0
                } else if (data.chol < Lpm311Data.CHOL_MG_MIN || data.chol > Lpm311Data.CHOL_MG_MAX) {
                    0.0
                } else if (data.hdl < Lpm311Data.HDL_MG_MIN || data.hdl > Lpm311Data.HDL_MG_MAX) {
                    0.0
                } else {
                    data.chol.div(data.hdl)
                }
            }
            else -> {
                data.ldl = 0.0
                data.cholDivHdl = 0.0
            }
        }
        data.cholStr = Lpm311Data.getDataStr(data.unit, Lpm311Data.CHOL, data.chol)
        data.hdlStr = Lpm311Data.getDataStr(data.unit, Lpm311Data.HDL, data.hdl)
        data.trigStr = Lpm311Data.getDataStr(data.unit, Lpm311Data.TRIG, data.trig)
        data.ldlStr = Lpm311Data.getDataStr(data.unit, Lpm311Data.LDL, data.ldl)
        data.cholDivHdlStr = Lpm311Data.getDataStr(data.unit, Lpm311Data.CHOL_HDL, data.cholDivHdl)

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
        GlobalScope.launch {
            delay(1000)
            sendCmd("connect".toByteArray(StandardCharsets.US_ASCII))
        }
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