package com.lepu.lepuble.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.base.BleInterface
import com.lepu.blepro.ble.Er1BleManager
import com.lepu.blepro.ble.OxyBleManager
import com.lepu.blepro.ble.cmd.BleCRC
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.cmd.UniversalBleCmd
import com.lepu.blepro.ble.data.Er1DataController
import com.lepu.blepro.ble.data.LepuDevice
import com.lepu.blepro.event.EventMsgConst
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import kotlin.experimental.inv

class Er1BleInterface(model: Int): BleInterface(model) {
    private val TAG: String = "Er1BleInterface"


    override fun initManager(context: Context, device: BluetoothDevice) {
        manager = OxyBleManager(context)
        manager.setConnectionObserver(this)
        manager.setNotifyListener(this)
        manager.connect(device)
            .useAutoConnect(true)
            .timeout(10000)
            .retry(3, 100)
            .done {
                LepuBleLog.d(TAG, "Device Init")
            }
            .enqueue()
    }



    /**
     * download a file, name come from filelist
     */
    var curFileName: String? = null
    var curFile: Er1BleResponse.Er1File? = null
    var fileList: Er1BleResponse.Er1FileList? = null

    override fun readFile(userId: String, fileName: String) {
        curFileName =fileName
        sendCmd(UniversalBleCmd.readFileStart(fileName.toByteArray(), 0))
    }

    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response: Er1BleResponse.Er1Response) {
//        LepuBleLog.d(TAG, "received: ${response.cmd}")
        when(response.cmd) {
            UniversalBleCmd.GET_INFO -> {
                val erInfo = LepuDevice(response.content)

                LepuBleLog.d(TAG, "get info success")
                LiveEventBus.get(EventMsgConst.ER1.EventEr1Info).post(erInfo)

            }

            UniversalBleCmd.RT_DATA -> {
                val rtData = Er1BleResponse.RtData(response.content)
//                model.hr.value = rtData.param.hr
//                model.duration.value = rtData.param.recordTime
//                model.lead.value = rtData.param.leadOn
//                model.battery.value = rtData.param.battery

                Er1DataController.receive(rtData.wave.wFs)
//                LepuBleLog.d(TAG, "ER1 Controller: ${Er1DataController.dataRec.size}")
                LiveEventBus.get(EventMsgConst.ER1.EventEr1RtData)
                    .post(rtData)
            }

            UniversalBleCmd.READ_FILE_LIST -> {
                fileList = Er1BleResponse.Er1FileList(response.content)
                LepuBleLog.d(TAG, fileList.toString())
            }

            UniversalBleCmd.READ_FILE_START -> {
                if (response.pkgType == 0x01.toByte()) {
                    curFile = Er1BleResponse.Er1File(curFileName!!, toUInt(response.content))
                    sendCmd(UniversalBleCmd.readFileData(0))
                } else {
                    LepuBleLog.d(TAG, "read file failed：${response.pkgType}")
                }
            }

            UniversalBleCmd.READ_FILE_DATA -> {
                curFile?.apply {
                    this.addContent(response.content)
                    LepuBleLog.d(TAG, "read file：${curFile?.fileName}   => ${curFile?.index} / ${curFile?.fileSize}")

                    if (this.index < this.fileSize) {
                        sendCmd(UniversalBleCmd.readFileData(this.index))
                    } else {
                        sendCmd(UniversalBleCmd.readFileEnd())
                    }
                }
            }

            UniversalBleCmd.READ_FILE_END -> {
                LepuBleLog.d(TAG, "read file finished: ${curFile?.fileName} ==> ${curFile?.fileSize}")
                curFileName = null
                curFile = null
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun hasResponse(bytes: ByteArray?): ByteArray? {
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)
            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = Er1BleResponse.Er1Response(temp)
//                LepuBleLog.d(TAG, "get response: ${temp.toHex()}" )
                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }

    /**
     * get device info
     */
    override fun getInfo() {
        sendCmd(UniversalBleCmd.getInfo())
    }

    override fun syncData(type: String, value: Int) {
        TODO("Not yet implemented")
    }



    override fun resetDeviceInfo() {
        TODO("Not yet implemented")
    }

    /**
     * get real-time data
     */
    override fun getRtData() {
        sendCmd(UniversalBleCmd.getRtData())
    }


    /**
     * get file list
     */
    public fun getFileList() {
        sendCmd(UniversalBleCmd.getFileList())
    }


}