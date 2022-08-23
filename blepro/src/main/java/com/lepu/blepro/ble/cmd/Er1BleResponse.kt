package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.ble.data.Er1DataController
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.toUInt
import kotlinx.android.parcel.Parcelize

object Er1BleResponse {

    
    @Parcelize
    class Er1Response constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var pkgType: Byte
        var pkgNo: Int
        var len: Int
        var content: ByteArray

        init {
            cmd = (bytes[1].toUInt() and 0xFFu).toInt()
            pkgType = bytes[3]
            pkgNo = (bytes[4].toUInt() and 0xFFu).toInt()
            len = toUInt(bytes.copyOfRange(5, 7))
            content = bytes.copyOfRange(7, 7 + len)
        }
    }


    @Parcelize
    class RtData constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    @Parcelize
    class RtParam constructor(var bytes: ByteArray) : Parcelable {
        var hr: Int
        var sysFlag: Byte
        var rFlag: Boolean       // 有无检测到R波
        var badSignal: Boolean   // 信号差标记(主机算法重新初始化)
        var poorSignal: Boolean  // 信号弱标记(测量过程中信号弱)
        var batteryState: Int    // 电池状态 0：正常使用，1：充电中，2：充满，3：低电量
        var battery: Int         // 0-100%
        var recordTime: Int = 0  // 记录时长 s
        var runStatus: Byte
        var curStatus: Int       // 当前状态 0：空闲待机(导联脱落)，1：测量准备(主机丢弃前段波形阶段)，2：记录中，3：分析存储中，
                                 //         4：已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)，5：记录小于30s(记录中状态直接切换至此状态)，
                                 //         6：重测已达6次，进入待机，7：导联断开
        var lastStatus: Int       // 上一次状态（同上）
        var leadOn: Boolean
        // reserve 11

        init {
            hr = toUInt(bytes.copyOfRange(0, 2))
            sysFlag = bytes[2]
            rFlag = (byte2UInt(sysFlag) and 0x01) == 1
            badSignal = ((byte2UInt(sysFlag) and 0x02) shr 1) == 1
            poorSignal = ((byte2UInt(sysFlag) and 0x04) shr 2) == 1
            batteryState = (byte2UInt(sysFlag) and 0xC0) shr 6
            battery = (bytes[3].toUInt() and 0xFFu).toInt()
            if (bytes[8].toUInt() and 0x02u == 0x02u) {
                recordTime = toUInt(bytes.copyOfRange(4, 8))
            }
            runStatus = bytes[8]
            curStatus = byte2UInt(runStatus) and 0x0F
            lastStatus = (byte2UInt(runStatus) and 0xF0) shr 4
            leadOn = (bytes[8].toUInt() and 0x07u) != 0x07u
        }

        override fun toString(): String {
            return """
                RtParam:
                sysFlag: $sysFlag
                rFlag: $rFlag
                badSignal: $badSignal
                poorSignal: $poorSignal
                batteryState: $batteryState
                battery: $battery
                recordTime: $recordTime
                runStatus: $runStatus
                curStatus: $curStatus
                lastStatus: $lastStatus
                leadOn: $leadOn
            """.trimIndent()
        }

    }

    @Parcelize
    class RtWave constructor(var bytes: ByteArray) : Parcelable {
        var content: ByteArray = bytes
        var len: Int
        var wave: ByteArray
        var wFs : FloatArray

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            wave = bytes.copyOfRange(2, bytes.size)

            if (len != wave.size.div(2)) {
                len = wave.size.div(2)
            }

            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs[i] = Er1DataController.byteTomV(wave[2 * i], wave[2 * i + 1])
            }
        }
    }

    
    class Er3RtData constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var param: RtParam
        var wave: Er3RtWave

        init {
            param = RtParam(bytes.copyOfRange(0, 20))
            wave = Er3RtWave(bytes.copyOfRange(20, bytes.size))
        }
    }

    
    class Er3RtWave constructor(var bytes: ByteArray) {
        var content: ByteArray = bytes
        var len: Int
        val lead = 4
        var wave = mutableListOf<ByteArray>()
        var wFs : FloatArray

        init {
            len = toUInt(bytes.copyOfRange(0, 2))
            val lead1 = mutableListOf<Byte>()
            val lead2 = mutableListOf<Byte>()
            val lead3 = mutableListOf<Byte>()
            val lead4 = mutableListOf<Byte>()
            for (i in 2 until bytes.size step 8) {
                lead1.add(bytes[i])
                lead1.add(bytes[i + 1])
                lead2.add(bytes[i + 2])
                lead2.add(bytes[i + 3])
                lead3.add(bytes[i + 4])
                lead3.add(bytes[i + 5])
                lead4.add(bytes[i + 6])
                lead4.add(bytes[i + 7])
            }
            wave.add(lead1.toByteArray())
            wave.add(lead2.toByteArray())
            wave.add(lead3.toByteArray())
            wave.add(lead4.toByteArray())

            wFs = FloatArray(len)
            for (i in 0 until len) {
                wFs[i] = Er1DataController.byteTomV(lead1[2 * i], lead1[2 * i + 1])
            }
        }
    }


    class Er1File(val model: Int, val name: String, val size: Int, private val userId: String, var index: Int) {
        var fileName: String
        var fileSize: Int
        var content: ByteArray

        init {
            fileName = name
            fileSize = size
            content = ByteArray(size)
            index = index
        }

        fun addContent(bytes: ByteArray) {
            if (index >= fileSize) {
                LepuBleLog.d("index >= fileSize index:$index, fileSize:$fileSize 已下载完成")
                return // 已下载完成
            } else {
                System.arraycopy(bytes, 0, content, index, bytes.size)
                DownloadHelper.writeFile(model, userId, fileName, "dat", bytes)

                index += bytes.size
            }
            LepuBleLog.d("er1File, bytes size = ${bytes.size}, index = $index")
        }
    }

    
    @Parcelize
    class Er1FileList constructor(var bytes: ByteArray) : Parcelable {
        var size: Int
        var fileList = mutableListOf<ByteArray>()

        init {
            size=byte2UInt(bytes[0])
            for (i in  0 until size) {
                fileList.add(bytes.copyOfRange(1 + i * 16, 17 + i * 16))
            }
        }

        override fun toString(): String {
            var str = ""
            for (bs in fileList) {
                str += String(bs)
                str += ","
            }
            return str
        }
    }
}