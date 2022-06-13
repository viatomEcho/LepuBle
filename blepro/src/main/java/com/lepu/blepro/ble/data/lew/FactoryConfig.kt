package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import java.nio.charset.StandardCharsets

class FactoryConfig() {

    var burnFlag = 7           // 烧录标记 e.g. bit0:SN bit1:硬件版本 bit2:Branch Code
    var snFlag = true
    var hwVFlag = true
    var branchCodeFlag = true
    var hwVersion = '0'        // 硬件版本 'A'-'Z'
    var branchCode = ""        // Branch编码 8位
    var snLen = 0              // SN⻓度(⼩于18) e.g. 10
    var sn = ""                // SN号

    constructor(bytes: ByteArray) : this() {
        var index = 0
        burnFlag = byte2UInt(bytes[index])
        snFlag = (burnFlag and 0x01) == 1
        hwVFlag = ((burnFlag and 0x02) shr 1) == 1
        branchCodeFlag = ((burnFlag and 0x04) shr 2) == 1
        index++
        hwVersion = bytes[index].toChar()
        index++
        branchCode = trimStr(String(bytes.copyOfRange(index, index+8)))
        index += 8
        snLen = byte2UInt(bytes[index])
        index++
        sn = trimStr(String(bytes.copyOfRange(index, index+snLen)))
    }

    fun getDataBytes(): ByteArray {
        val snTemp = ByteArray(18)
        val snBytes = sn.toByteArray(StandardCharsets.US_ASCII)
//        val snBytes = sn.toByteArray(StandardCharsets.UTF_8)
        snLen = snBytes.size
        System.arraycopy(snBytes, 0, snTemp, 0, snLen)

        if (snFlag) {
            burnFlag = burnFlag or 0x01
        }
        if (hwVFlag) {
            burnFlag = burnFlag or 0x02
        }
        if (branchCodeFlag) {
            burnFlag = burnFlag or 0x04
        }

        return byteArrayOf(burnFlag.toByte())
            .plus(hwVersion.toByte())
            .plus(branchCode.toByteArray(StandardCharsets.US_ASCII))
//            .plus(branchCode.toByteArray(StandardCharsets.UTF_8))
            .plus(snLen.toByte())
            .plus(snTemp)
    }

    override fun toString(): String {
        return """
            FactoryConfig : 
            bytes : ${bytesToHex(getDataBytes())}
            burnFlag : $burnFlag
            snFlag : $snFlag
            hwVFlag : $hwVFlag
            branchCodeFlag : $branchCodeFlag
            hwVersion : $hwVersion
            branchCode : $branchCode
            snLen : $snLen
            sn : $sn
        """.trimIndent()
    }

}