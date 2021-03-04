package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.cmd.Er1BleCmd.FACTORY_RESET
import com.lepu.blepro.ble.data.Er2DeviceInfo
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import java.util.*
import kotlin.concurrent.schedule
import kotlin.experimental.inv

/**
 * author: wujuan
 * created on: 2021/2/26 13:57
 * description:
 */
class Er2BleInterface(model: Int): BleInterface(model) {
    private val tag: String = "Er2BleInterface"
    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = Er2BleManager(context)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(false)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(tag, "Device Init")
            }
            .enqueue()
    }



    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    private var userId: String? = null
    var curFile: Er2File? = null

    override fun dealReadFile(userId: String, fileName: String) {
        this.userId = userId
        this.curFileName =fileName
        LepuBleLog.d(tag, "dealReadFile:: $userId, $fileName, offset = $offset")
        sendCmd(Er2BleCmd.readFileStart(fileName.toByteArray(), this.offset))
    }



    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i + 1] != bytes[i + 2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i + 5, i + 7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i + 8 + len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val respPkg = Er2BleResponse(temp)
                onResponseReceived(respPkg)
                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(
                    i + 8 + len,
                    bytes.size
                )

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }


    private fun onResponseReceived(respPkg: Er2BleResponse) {
        when(respPkg.cmd) {
            Er2BleCmd.CMD_RETRIEVE_DEVICE_INFO -> {
                val info = Er2DeviceInfo(device.name, device.address, respPkg.data)

                LepuBleLog.d(tag, "model:$model,GET_INFO => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2Info).post(InterfaceEvent(model, info))

                if (runRtImmediately){
                    runRtTask()
                    runRtImmediately = false
                }
            }
            Er2BleCmd.CMD_SET_TIME -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_TIME => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2SetTime).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Er2BleCmd.CMD_SET_SWITCHER_STATE -> {

                LepuBleLog.d(tag, "model:$model,CMD_SET_SWITCHER_STATE => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2SetSwitcherState).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Er2BleCmd.CMD_RETRIEVE_SWITCHER_STATE -> {

                LepuBleLog.d(tag, "model:$model,CMD_RETRIEVE_SWITCHER_STATE => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2SwitcherState).post(
                    InterfaceEvent(
                        model,
                        respPkg.data
                    )
                )
            }
            Er2BleCmd.CMD_FACTORY_RESET -> {

                LepuBleLog.d(tag, "model:$model,CMD_FACTORY_RESET => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2FactoryReset).post(
                    InterfaceEvent(
                        model,
                        true
                    )
                )
            }
            Er2BleCmd.CMD_GET_REAL_TIME_DATA -> {

                val rtData = Er2RtData(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_GET_REAL_TIME_DATA => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2RtData).post(
                    InterfaceEvent(
                        model,
                        rtData
                    )
                )
            }
            Er2BleCmd.CMD_LIST_FILE -> {
                val fileArray = Er2FileList(respPkg.data)

                LepuBleLog.d(tag, "model:$model,CMD_GET_REAL_TIME_DATA => success")
                LiveEventBus.get(InterfaceEvent.ER2.EventEr2FileList).post(
                    InterfaceEvent(
                        model,
                        fileArray
                    )
                )
            }
            Er2BleCmd.CMD_START_READ_FILE -> {

                if (respPkg.pkgType == 0x01.toByte()) {
                    curFile =  curFileName?.let {
                        Er2File(model, it, toUInt(respPkg.buf), userId!!)
                    }
                    sendCmd(Er2BleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(tag, "read file failed：${respPkg.pkgType}")
                    LiveEventBus.get(InterfaceEvent.ER2.EventEr2ReadFileError).post(
                        InterfaceEvent(
                            model,
                            true
                        )
                    )
                }

            }
            Er2BleCmd.CMD_READ_FILE_CONTENT -> {
                curFile?.apply {

                    LepuBleLog.d(tag, "CMD_READ_FILE_CONTENT: paused = $isPausedRF, cancel = $isCancelRF, offset =  ${offset}, index = ${this.index}")

                    //检查当前的下载状态
                    if (isCancelRF || isPausedRF) {
                        sendCmd(Er1BleCmd.readFileEnd())
                        return
                    }

                    this.addContent(respPkg.buf, offset)
                    LepuBleLog.d(tag, "read file：${this.fileName}   => ${this.index + offset} / ${this.fileSize}")
                    LepuBleLog.d(tag, "read file：${((this.index+ offset) * 1000).div(this.fileSize) }")
                    LiveEventBus.get(InterfaceEvent.ER2.EventEr2ReadingFileProgress).post(InterfaceEvent(model, ((this.index+ offset) * 1000).div(this.fileSize) ))

                    if (this.index + offset < this.fileSize) {
                        sendCmd(Er2BleCmd.readFileData(this.index))
                    } else {
                        sendCmd(Er2BleCmd.readFileEnd())
                    }
                }
            }
            Er2BleCmd.CMD_END_READ_FILE -> {
                LepuBleLog.d(tag, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                LepuBleLog.d(tag, "read file finished: isCancel = $isCancelRF, isPause = $isPausedRF")

                curFileName = null// 一定要放在发通知之前
                curFile?.let {
                    if (isCancelRF || isPausedRF) return
                    LiveEventBus.get(InterfaceEvent.ER2.EventEr2ReadFileComplete).post(InterfaceEvent(model, it))
                }?: LepuBleLog.d(tag, "READ_FILE_END  model:$model,  curFile error!!")
                curFile = null
            }

            else -> {

            }
        }
    }
    fun setSwitcherState(hrFlag: Boolean) {
        sendCmd( Er2BleCmd.setSwitcherState(hrFlag))
    }

    fun getSwitcherState() {
        sendCmd(Er2BleCmd.getSwitcherState())
    }

    override fun getInfo() {
        sendCmd(Er2BleCmd.getDeviceInfo())
    }

    override fun syncTime() {
        sendCmd(Er2BleCmd.setTime())
    }

    override fun getRtData() {
        sendCmd(Er2BleCmd.getRtData())
    }

    override fun getFileList() {
        sendCmd(Er2BleCmd.listFiles())
    }



    override fun resetDeviceInfo() {
        sendCmd(Er2BleCmd.factoryReset())
    }

    override fun dealContinueRF(userId: String, fileName: String) {
    }



}