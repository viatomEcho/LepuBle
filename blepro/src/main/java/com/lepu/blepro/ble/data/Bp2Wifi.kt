package com.lepu.blepro.ble.data

import com.lepu.blepro.utils.HexString.hexToBytes
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.bytesToHex
import java.nio.charset.Charset

class Bp2Wifi() {

    var length = 0

    var state: Int = 0           // 0:断开 1:连接中 2:已连接 0xff:密码错误 0xfd:找不到SSID
    var ssidLen: Int = 0
    var ssid: String = ""         // ssid
    var type: Int = 0            // wifi类型	0:2.4G   1:5G
    var rssi: Int = 0            // 信号强度
    var pwdLen: Int = 0
    var pwd: String = ""
    var macAddr: String = ""      // wifi模块mac地址
    var ipType: Int = 0          // ip类型 0动态 1静态
    var ipLen: Int = 0
    var ipAddr: String = ""       // ip信息
    var netmaskLen: Int = 0
    var netmaskAddr: String = ""  // 子网掩码
    var gatewayLen: Int = 0
    var gatewayAddr: String = ""  // 网关
    var bytes = byteArrayOf(0)

    constructor(i: Int, bytes: ByteArray) : this() {
        this.bytes = bytes
        var index = i
        state = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        ssidLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        ssid = trimStr(String(bytes.copyOfRange(index, index+ssidLen), Charset.defaultCharset()))
        index += ssidLen
        type = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        rssi = bytes[index].toInt()
        index++
        pwdLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        pwd = trimStr(String(bytes.copyOfRange(index, index+pwdLen), Charset.defaultCharset()))
        index += pwdLen
        macAddr = bytesToHex(bytes.copyOfRange(index, index+6))
        index += 6
        ipType = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        ipLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        ipAddr = trimStr(String(bytes.copyOfRange(index, index+ipLen), Charset.defaultCharset()))
        index += ipLen
        netmaskLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        netmaskAddr = trimStr(String(bytes.copyOfRange(index, index+netmaskLen), Charset.defaultCharset()))
        index += netmaskLen
        gatewayLen = (bytes[index].toUInt() and 0xFFu).toInt()
        index++
        gatewayAddr = trimStr(String(bytes.copyOfRange(index, index+gatewayLen), Charset.defaultCharset()))
        index += gatewayLen
        length = index - i
    }

    fun getDataBytes(): ByteArray {
        val data = byteArrayOf(state.toByte())
        if (pwd.isEmpty()) {
            pwd = "a"
        }
        return data.plus(ssid.toByteArray(Charset.defaultCharset()).size.toByte())
            .plus(ssid.toByteArray(Charset.defaultCharset()))
            .plus(type.toByte())
            .plus(rssi.toByte())
            .plus(pwd.toByteArray(Charset.defaultCharset()).size.toByte())
            .plus(pwd.toByteArray(Charset.defaultCharset()))
            .plus(hexToBytes(macAddr))
            .plus(ipType.toByte())
            .plus(ipLen.toByte())
            .plus(ipAddr.toByteArray(Charset.defaultCharset()))
            .plus(netmaskAddr.toByteArray(Charset.defaultCharset()).size.toByte())
            .plus(netmaskAddr.toByteArray(Charset.defaultCharset()))
            .plus(gatewayAddr.toByteArray(Charset.defaultCharset()).size.toByte())
            .plus(gatewayAddr.toByteArray(Charset.defaultCharset()))
    }

    override fun toString(): String {
        return """
            Bp2Wifi : 
            bytes : ${bytesToHex(bytes)}
            getDataBytes : ${bytesToHex(getDataBytes())}
            state : $state
            ssidLen : $ssidLen
            ssid : $ssid
            type : $type
            rssi : $rssi
            pwdLen : $pwdLen
            pwd : $pwd
            macAddr : $macAddr
            ipType : $ipType
            ipLen : $ipLen
            ipAddr : $ipAddr
            netmaskLen : $netmaskLen
            netmaskAddr : $netmaskAddr
            gatewayLen : $gatewayLen
            gatewayAddr : $gatewayAddr
        """.trimIndent()
    }
}