package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.ResponseError
import com.lepu.blepro.ble.cmd.TmbBleResponse
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.ByteUtils.int4BytesBig
import com.lepu.blepro.utils.DateUtil
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.toUInt

/**
 * 蓝牙操作
 * 通讯流程：连接设备后，下位机发送请求登录指令，上位机发送应答ACK，发送登录状态，下位机发送应答ACK
 *         上位机发送同步时间，下位机发送应答ACK，发送设置状态，上位机发送应答ACK
 *         上位机发送同步数据，下位机发送应答ACK，发送数据，上位机发送应答ACK
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
                (manager as TmbBleManager).readInfo()
                LepuBleLog.d(tag, "manager.connect done")
            }
            .enqueue()
    }

    private var record = ByteArray(0)

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: TmbBleResponse.BleResponse) {
        LepuBleLog.d(tag, "onResponseReceived : $response")
        when (response.cmd) {
            // 设备请求login
            0x0007 -> {
                // 应答ACK
                sendCmdAck()
                val data = TmbBleResponse.Login(response.content)
                (manager as TmbBleManager).tmbInfo.userId = data.userId
                (manager as TmbBleManager).tmbInfo.deviceId = bytesToHex(data.deviceId)
                (manager as TmbBleManager).tmbInfo.battery = data.battery
                val login = byteArrayOf(0x10.toByte(), 0x0B.toByte(), 0x00.toByte(), 0x08.toByte(), 0x01.toByte())
                    .plus(data.deviceId)
                    .plus(0x00.toByte())
                    .plus(0x02.toByte())
                // 应答login
                sendCmd(login)
            }
            // 设备应答同步时间
            0x1100 -> {
                // 应答ACK
                if (response.content.size > 2 && byte2UInt(response.content[2]) == 0x01) {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.TMB.EventTmbSetTime).post(InterfaceEvent(model, true))
                } else {
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.TMB.EventTmbSetTime).post(InterfaceEvent(model, false))
                }
                sendCmdAck()
            }
            // 设备发送数据第一包
            0x4902 -> {
                record = response.content
            }
            else -> {
                // 设备发送数据第二包
                if (response.head == 0x21) {
                    // 应答ACK
                    sendCmdAck()
                    record = record.plus(response.content)
                    val data = TmbBleResponse.Record(record)
                    LepuBleLog.d(tag, "onResponseReceived : $data")
                    LiveEventBus.get<InterfaceEvent>(InterfaceEvent.TMB.EventTmbRecordData).post(InterfaceEvent(model, data))
                } else {
                    // 设备应答ACK
                    if (toUInt(response.content) != 0x01) {
                        val data = ResponseError()
                        data.model = model
                        data.type = toUInt(response.content)
                        LiveEventBus.get<ResponseError>(EventMsgConst.Cmd.EventCmdResponseError).post(data)
                        LepuBleLog.d(tag, "onResponseReceived ResponseError : $data")
                    }
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
        sendCmd(byteArrayOf(0x10.toByte(), 0x04.toByte(), 0x49.toByte(), 0x01.toByte(), (manager as TmbBleManager).tmbInfo.userId.toByte(), 0x01.toByte()))
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