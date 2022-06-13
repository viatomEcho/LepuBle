package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.ble.cmd.LewBleCmd
import com.lepu.blepro.utils.DateUtil.stringFromDate
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int4ByteArray
import java.nio.charset.StandardCharsets
import java.util.*

class NotificationInfo {

    var appId = 0          // LewBleCmd.AppId
    var time = 0           // 时间戳s
    // reserve 5
    var info: Any? = null

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(appId.toByte())
            .plus(int4ByteArray(time))
        when (appId) {
            LewBleCmd.AppId.PHONE -> {
                data.plus((info as NotiPhone).getDataBytes())
            }
            LewBleCmd.AppId.MESSAGE -> {
                data.plus((info as NotiMessage).getDataBytes())
            }
            else -> {
                data.plus((info as NotiOther).getDataBytes())
            }
        }
        return data
    }

    override fun toString(): String {
        return """
            NotificationInfo : 
            bytes : ${bytesToHex(getDataBytes())}
            appId : $appId
            time : $time
            timeStr : ${stringFromDate(Date(time*1000L), "yyyy-MM-dd HH:mm:ss")}
            info : ${when (appId) {
                LewBleCmd.AppId.PHONE -> info as NotiPhone
                LewBleCmd.AppId.MESSAGE -> info as NotiMessage
                else -> info as NotiOther
            }}
        """.trimIndent()
    }

    class NotiPhone {
        var status = 0    // LewBleCmd.PhoneStatus
        var nameLen = 0
        var name = ""
        var phoneLen = 0
        var phone = ""
        fun getDataBytes(): ByteArray {
//            val nameTemp = name.toByteArray(StandardCharsets.US_ASCII)
            val nameTemp = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameTemp.size
            val phoneTemp = phone.toByteArray(StandardCharsets.US_ASCII)
//            val phoneTemp = phone.toByteArray(StandardCharsets.UTF_8)
            phoneLen = phoneTemp.size

            return byteArrayOf(status.toByte())
                .plus(nameLen.toByte())
                .plus(nameTemp)
                .plus(phoneLen.toByte())
                .plus(phoneTemp)
        }
        override fun toString(): String {
            return """
                NotiPhone : 
                bytes : ${bytesToHex(getDataBytes())}
                status : $status
                nameLen : $nameLen
                name : $name
                phoneLen : $phoneLen
                phone : $phone
            """.trimIndent()
        }
    }

    class NotiMessage {
        var nameLen = 0
        var name = ""
        var phoneLen = 0
        var phone = ""
        var textLen = 0
        var text = ""
        fun getDataBytes(): ByteArray {
//            val nameTemp = name.toByteArray(StandardCharsets.US_ASCII)
            val nameTemp = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameTemp.size
            val phoneTemp = phone.toByteArray(StandardCharsets.US_ASCII)
//            val phoneTemp = phone.toByteArray(StandardCharsets.UTF_8)
            phoneLen = phoneTemp.size
//            val textTemp = text.toByteArray(StandardCharsets.US_ASCII)
            val textTemp = text.toByteArray(StandardCharsets.UTF_8)
            textLen = textTemp.size

            return byteArrayOf(nameLen.toByte())
                .plus(nameTemp)
                .plus(phoneLen.toByte())
                .plus(phoneTemp)
                .plus(textLen.toByte())
                .plus(textTemp)
        }
        override fun toString(): String {
            return """
                NotiMessage : 
                bytes : ${bytesToHex(getDataBytes())}
                nameLen : $nameLen
                name : $name
                phoneLen : $phoneLen
                phone : $phone
                textLen : $textLen
                text : $text
            """.trimIndent()
        }
    }

    class NotiOther {
        var nameLen = 0
        var name = ""
        var textLen = 0
        var text = ""
        fun getDataBytes(): ByteArray {
//            val nameTemp = name.toByteArray(StandardCharsets.US_ASCII)
            val nameTemp = name.toByteArray(StandardCharsets.UTF_8)
            nameLen = nameTemp.size
//            val textTemp = text.toByteArray(StandardCharsets.US_ASCII)
            val textTemp = text.toByteArray(StandardCharsets.UTF_8)
            textLen = textTemp.size

            return byteArrayOf(nameLen.toByte())
                .plus(nameTemp)
                .plus(textLen.toByte())
                .plus(textTemp)
        }
        override fun toString(): String {
            return """
                NotiOther : 
                bytes : ${bytesToHex(getDataBytes())}
                nameLen : $nameLen
                name : $name
                textLen : $textLen
                text : $text
            """.trimIndent()
        }
    }

}