package com.lepu.blepro.ble.cmd
import android.os.Parcelable
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize
import org.json.JSONObject
//
class OxyBleResponse{

    @ExperimentalUnsignedTypes
    class OxyResponse(bytes: ByteArray) {
        var no:Int
        var len: Int
        var  state: Boolean
        var content: ByteArray

        init {
            state = bytes[1].toInt() == 0x00
            no = toUInt(bytes.copyOfRange(3, 5))
            len = bytes.size - 8
            content = bytes.copyOfRange(7, 7 + len)
        }
    }


    @ExperimentalUnsignedTypes
    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int
        var pr: Int
        var battery: Int
        var batteryState: String // 0 -> not charging; 1 -> charging; 2 -> charged
        var pi: Int
        var state: String //1-> lead on; 0-> lead off; other
        var len: Int
        var waveByte: ByteArray
        var wFs: IntArray? = null
        var wByte: ByteArray? = null

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr = toUInt(bytes.copyOfRange(1, 3))
            battery = bytes[3].toUInt().toInt()
            batteryState = bytes[4].toUInt().toString()
            pi = ByteUtils.byte2UInt(bytes[5])
            state = bytes[6].toUInt().toString()
            len = toUInt(bytes.copyOfRange(10, 12))
            waveByte = bytes.copyOfRange(12, 12 + len)
            wFs = IntArray(len)
            wByte = ByteArray(len)
            for (i in 0 until len) {
                var temp = ByteUtils.byte2UInt(waveByte[i])
                if (temp == 156) {
                    if (i==0) {
                        temp = ByteUtils.byte2UInt(waveByte[i+1])
                    } else if (i == len-1) {
                        temp = ByteUtils.byte2UInt(waveByte[i-1])
                    } else {
                        temp = (ByteUtils.byte2UInt(waveByte[i-1]) + ByteUtils.byte2UInt(waveByte[i+1]))/2
                    }
                }

                wFs!![i] = temp
                wByte!![i] = (100 - temp/2).toByte()
            }
        }
    }

    @ExperimentalUnsignedTypes
    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var spo2: Int
        var battery: Int
        var batteryState: String // 0 -> not charging; 1 -> charging; 2 -> charged
        var pi: Int
        var state: String //1-> lead on; 0-> lead off; other
        var steps: Int
        var pr: Int
        var vector: Int

        init {
            spo2 = bytes[0].toUInt().toInt()
            pr =  (bytes[1].toUInt().toInt() and 0xFF) or (bytes[2].toUInt().toInt() and 0xFF shl 8)
            steps = bytes[3].toUInt().toInt() and 0xFF or (bytes[4].toUInt().toInt() and 0xFF shl 8) or (bytes[5].toUInt().toInt() and 0xFF shl 16) or (bytes[6].toUInt().toInt() and 0xFF shl 24)
            battery = bytes[7].toUInt().toInt()
            batteryState = bytes[8].toUInt().toString()
            vector = bytes[9].toUInt().toInt()
            pi = bytes[10].toUInt().toInt() and 0xFF
            state = bytes[11].toUInt().toString()
        }
    }

    @Parcelize
    class OxyInfo (val bytes: ByteArray) : Parcelable {
        var region: String
        var model: String
        var hwVersion: String // hardware version
        var swVersion: String // software version
        var btlVersion: String
        var pedTar: Int
        var sn: String
        var curTime: String
        //        var battery: Int
        var batteryState: String  // 0 -> not charging; 1 -> charging; 2 -> charged
        var batteryValue:String
        var oxiThr: Int
        var motor: String
        var mode: String
        var fileList: String
        var oxiSwitch:Int
        var hrSwitch:Int
        var hrLowThr:Int
        var hrHighThr:Int
        init {
            var infoStr = JSONObject(String(bytes))
//            try {
//                var infoStr = JSONObject(String(bytes))
//            } catch (e: JSONException) {
//                LogUtils.d(String(bytes))
//            }

            region = infoStr.getString("Region")
            model = infoStr.getString("Model")
            hwVersion = infoStr.getString("HardwareVer")
            swVersion = infoStr.getString("SoftwareVer")
            btlVersion = infoStr.getString("BootloaderVer")
            pedTar = infoStr.getString("CurPedtar").toInt()
            sn = infoStr.getString("SN")
            curTime = infoStr.getString("CurTIME")
            //            battery = infoStr.getString("CurBAT").toIntOrNull() // 100%, 难解，不管
            batteryState = infoStr.getString("CurBatState")
            batteryValue = infoStr.getString("CurBAT")
            oxiSwitch = infoStr.getInt("OxiSwitch")
            oxiThr = infoStr.getString("CurOxiThr").toInt()
            motor = infoStr.getString("CurMotor")
            mode = infoStr.getString("CurMode")
            fileList = infoStr.getString("FileList")
            hrSwitch = infoStr.getInt("HRSwitch")
            hrLowThr = infoStr.getInt("HRLowThr")
            hrHighThr = infoStr.getInt("HRHighThr")

        }

        override fun toString(): String {
            return "OxyInfo(bytes=${bytes.contentToString()}, region='$region', model='$model', hwVersion='$hwVersion', swVersion='$swVersion', btlVersion='$btlVersion', pedTar=$pedTar, sn='$sn', curTime='$curTime', batteryState='$batteryState', oxiThr=$oxiThr, motor='$motor', mode='$mode', fileList='$fileList', oxiSwitch=$oxiSwitch, hrSwitch=$hrSwitch)"
        }

    }

    @Parcelize
    class OxyFile(val model: Int, val name: String, val size: Int, private val userId: String) : Parcelable  {
        var fileName: String
        var fileSize: Int
        var fileContent: ByteArray
        var index: Int  // 标识当前下载index

        init {
            fileName = name
            fileSize = size
            fileContent = ByteArray(size)
            index = 0
        }


        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                LepuBleLog.d("index > fileSize. 文件下载完成")
                return
            } else {
                System.arraycopy(bytes, 0, fileContent, index, bytes.size)
                DownloadHelper.writeFile(model, userId, fileName, "dat", bytes )

                index += bytes.size

            }
        }
    }

}