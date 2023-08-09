package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.TmbBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.int4BytesBig
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex

/**
 *
 * 蓝牙操作
 */
class TmbBleInterface(model: Int): BleInterface(model) {
    private val tag: String = "TmbBleInterface"

    override fun initManager(context: Context, device: BluetoothDevice, isUpdater: Boolean) {
        if (isManagerInitialized()) {
            if (manager.bluetoothDevice == null) {
                manager = TmbBleManager(context)
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice == null")
                LepuBleLog.d(tag, "isManagerInitialized, manager.create done")
            } else {
                LepuBleLog.d(tag, "isManagerInitialized, manager.bluetoothDevice != null")
            }
        } else {
            manager = TmbBleManager(context)
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
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    private var id = 0
    private var mac = ByteArray(0)
    private var record = ByteArray(0)

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: TmbBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived : $response")
        when (response.cmd) {
            0x0007 -> {
                sendCmdAck()
                id = byte2UInt(response.content[6])
                mac = response.content.copyOfRange(0, 6)
                val login = byteArrayOf(0x10.toByte(), 0x0B.toByte(), 0x00.toByte(), 0x08.toByte(), 0x01.toByte())
                    .plus(mac)
                    .plus(0x00.toByte())
                    .plus(0x02.toByte())
                sendCmd(login)
            }
            0x1100 -> {
                sendCmdAck()
            }
            0x4902 -> {
                record = response.content
            }
            else -> {
                if (response.head == 0x21) {
                    sendCmdAck()
                    record = record.plus(response.content)
                    LepuBleLog.d(tag, "onResponseReceived : ${TmbBleResponse.Record(record)}")
                }
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {

        val bytesLeft: ByteArray? = bytes
        if (bytes == null || bytes.size < 2) {
            return bytes
        }
        LepuBleLog.d(tag, "hasResponse : ${bytesToHex(bytes)}")
        loop@ for (i in 0 until bytes.size-1) {
            if (bytes[i] == 0x10.toByte() || bytes[i] == 0x20.toByte() || bytes[i] == 0x21.toByte() || (bytes[i] == 0x00.toByte() && bytes[i+1] == 0x01.toByte())) {
                // need content length
                val len = byte2UInt(bytes[i+1])
                LepuBleLog.d(tag, "len : $len")
                if (i+2+len > bytes.size) {
                    continue@loop
                }

                val temp: ByteArray = bytes.copyOfRange(i, i+2+len)
                onResponseReceived(TmbBleResponse.BleResponse(temp))

                val tempBytes: ByteArray? = if (i+2+len == bytes.size) null else bytes.copyOfRange(i+2+len, bytes.size)

                return hasResponse(tempBytes)
            } else {
                continue@loop
            }
        }
        return bytesLeft
    }

    private fun sendCmdAck() {
        (manager as TmbBleManager).sendCmdAck(byteArrayOf(0x00.toByte(), 0x01.toByte(), 0x01.toByte()))
    }

    override fun getFileList() {
        sendCmd(byteArrayOf(0x10.toByte(), 0x04.toByte(), 0x49.toByte(), 0x01.toByte(), id.toByte(), 0x01.toByte()))
    }

    override fun syncTime() {
        val offset = DateUtil.getTimeZoneOffset().div(1000)
        // 设备基准时间：2010-01-01 00:00:00，对应时间戳是1262304000
        val time = int4BytesBig(System.currentTimeMillis().div(1000)-1262304000+offset)
        sendCmd(byteArrayOf(0x10.toByte(), 0x07.toByte(), 0x11.toByte(), 0x02.toByte(), 0x01.toByte()).plus(time))
    }

    override fun getInfo() {
        val data = (manager as TmbBleManager).tmbInfo
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.TMB.EventTmbGetInfo).post(InterfaceEvent(model, data))
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