package com.lepu.blepro.ble.cmd

import android.os.Parcelable
import com.lepu.blepro.utils.*
import kotlinx.android.parcel.Parcelize

object MyScaleBleResponse {

    @ExperimentalUnsignedTypes
    @Parcelize
    class MyScaleResponse constructor(var bytes: ByteArray) : Parcelable {
        var cmd: Int
        var content: ByteArray  // 内容

        init {
            cmd = (bytes[18].toUInt() and 0xFFu).toInt()
            content = bytes.copyOfRange(2, 17)
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class WeightData constructor(var bytes: ByteArray) : Parcelable {
        var weightG: Int           // 体重g
        var kgScaleDivision: Int   // 分度值kg公斤
        var lbScaleDivision: Int   // 分度值lb英镑
        var isElectrode8: Boolean  // true 八电极 false 四电极
        var isSupportHR: Boolean       // 支持心率
        var isSupportBalance: Boolean  // 支持平衡
        var isSupportFocus: Boolean    // 支持重心
        var isStable: Boolean          // 稳定

        init {
            weightG = ((bytes[0].toInt() and 0xFF) shl 16) + ((bytes[1].toInt() and 0xFF) shl 8) + (bytes[2].toInt() and 0x03)
            kgScaleDivision = bytes[2].toInt() and 0x1C
            lbScaleDivision = bytes[2].toInt() and 0xE0
            isElectrode8 = (bytes[3].toInt() and 0x01) != 0
            isSupportHR = ((bytes[3].toInt() and 0x02) shr 1) != 0
            isSupportBalance = ((bytes[3].toInt() and 0x04) shr 2) != 0
            isSupportFocus = ((bytes[3].toInt() and 0x08) shr 3) != 0
            isStable = ((bytes[3].toInt() and 0x80) shr 7) != 0
        }

        override fun toString(): String {
            return """
                weightG = $weightG
                kgScaleDivision = $kgScaleDivision
                lbScaleDivision = $lbScaleDivision
                isElectrode8 = $isElectrode8
                isSupportHR = $isSupportHR
                isSupportBalance = $isSupportBalance
                isSupportFocus = $isSupportFocus
                isStable = $isStable
            """
        }
    }

    @Parcelize
    @ExperimentalUnsignedTypes
    class ImpedanceData constructor(var bytes: ByteArray) : Parcelable {
        var impNum: Int
        var seqNo: Int
        var imp: Int

        init {
            impNum = (bytes[0].toUInt() and 0xFFu).toInt()
            seqNo = (bytes[1].toUInt() and 0xFFu).toInt()
            imp = toUInt(bytes.copyOfRange(2, 4))
        }

        override fun toString(): String {
            return """
                impNum = $impNum
                seqNo = $seqNo
                imp = $imp
            """
        }
    }

}