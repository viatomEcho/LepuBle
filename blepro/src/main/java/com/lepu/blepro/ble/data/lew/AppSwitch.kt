package com.lepu.blepro.ble.data.lew

import com.lepu.blepro.utils.ByteUtils.byte2UInt
import com.lepu.blepro.utils.bytesToHex
import com.lepu.blepro.utils.int4ByteArray
import com.lepu.blepro.utils.toUInt

class AppSwitch() {

    var all = false
    var notification = 0
    var phone = false      // 0
    var message = false    // 1
    var qq = false         // 2
    var wechat = false     // 3
    var email = false      // 4
    var facebook = false   // 5
    var twitter = false    // 6
    var whatsApp = false   // 7
    var instagram = false  // 8
    var skype = false      // 9
    var linkedIn = false   // 10
    var line = false       // 11
    var weibo = false      // 12

    var other = false      // 31

    constructor(bytes: ByteArray) : this() {
        var index = 0
        all = byte2UInt(bytes[index]) == 1
        index++
        notification = toUInt(bytes.copyOfRange(index, index+4))
        phone = (notification and 0x00000001) == 1
        message = ((notification and 0x00000002) shr 1) == 1
        qq = ((notification and 0x00000004) shr 2) == 1
        wechat = ((notification and 0x00000008) shr 3) == 1
        email = ((notification and 0x00000010) shr 4) == 1
        facebook = ((notification and 0x00000020) shr 5) == 1
        twitter = ((notification and 0x00000040) shr 6) == 1
        whatsApp = ((notification and 0x00000080) shr 7) == 1
        instagram = ((notification and 0x00000100) shr 8) == 1
        skype = ((notification and 0x00000200) shr 9) == 1
        linkedIn = ((notification and 0x00000400) shr 10) == 1
        line = ((notification and 0x00000800) shr 11) == 1
        weibo = ((notification and 0x00001000) shr 12) == 1

        other = ((notification and 0x10000000) shr 31) == 1
    }

    fun getDataBytes(): ByteArray {
        val on = if (all) {
            1
        } else {
            0
        }
        if (phone) {
            notification = notification or 0x00000001
        }
        if (message) {
            notification = notification or 0x00000002
        }
        if (qq) {
            notification = notification or 0x00000004
        }
        if (wechat) {
            notification = notification or 0x00000008
        }
        if (email) {
            notification = notification or 0x00000010
        }
        if (facebook) {
            notification = notification or 0x00000020
        }
        if (twitter) {
            notification = notification or 0x00000040
        }
        if (whatsApp) {
            notification = notification or 0x00000080
        }
        if (instagram) {
            notification = notification or 0x00000100
        }
        if (skype) {
            notification = notification or 0x00000200
        }
        if (linkedIn) {
            notification = notification or 0x00000400
        }
        if (line) {
            notification = notification or 0x00000800
        }
        if (weibo) {
            notification = notification or 0x00001000
        }
        if (other) {
            notification = notification or 0x10000000
        }
        return byteArrayOf(on.toByte())
            .plus(int4ByteArray(notification))
    }

    override fun toString(): String {
        return """
            AppSwitch : 
            bytes : ${bytesToHex(getDataBytes())}
            all : $all
            notification : $notification
            phone : $phone
            message : $message
            qq : $qq
            wechat : $wechat
            email : $email
            facebook : $facebook
            twitter : $twitter
            whatsApp : $whatsApp
            instagram : $instagram
            skype : $skype
            linkedIn : $linkedIn
            line : $line
            weibo : $weibo
            other : $other
        """.trimIndent()
    }

}