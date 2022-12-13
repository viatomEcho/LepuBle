package com.lepu.blepro.vals


/**
 * relay info
 */
var relayId: String = "123456"
/**
 * 0x00 : 电池供电
 * 0x10 : 低电量
 * 0x01 : 充电中
 */
var batteryState : Int = 0
var battery = 0

/**
 * ER1
 */
public var hasEr1 = false
public var er1Name: String? = null
public var er1Sn: String? = null
public var er1Battery = 0
public var er1BleError = 0
public var er1RecordTime = 0
public var er1Conn = false
// lead info
var lead: Int = 0x02

/**
 * Oxy
 */
public var hasOxy = false
public var oxyName: String? = null
public var oxySn: String? = null
public var oxyBattery = 0
public var oxyBleError = 0
public var oxyRecordTime = 0
public var oxyConn = false

/**
 * KCA
 */
public var hasKca = false
public var kcaName: String? = null
public var kcaSn: String? = null
public var kcaBattery = 0
public var kcaBleError = 0
public var kcaRecordTime = 0
public var kcaConn = false

/**
 * 收发器电池数组
 */
val relayBatArr = arrayListOf<Int>(0,0,1,1,1,1,2,2,2,3,3,3,3,4,4,4,4,5,5,5,6,6,6,6,7,7,7,8,8,8,8,9,9,9,10,10,10,10,11,11,11,11,12,12,12,13,13,13,13,14,14,14,15,15,15,15,16,16,16,17,17,17,17,18,18,18,18,19,19,19,20,20,20,20,21,21,21,22,22,22,22,23,23,23,24,24,24,24,25,25,25,25,26,26,26,27,27,27,27,28,28)

/**
 * er1 电池数组
 */
val erBatArr =  arrayListOf<Int>(0,0,0,2,3,4,4,5,6,7,7,8,9,9,10,11,11,12,12,13,13,14,15,15,15,16,16,17,17,18,18,18,19,21,21,22,22,22,22,23,23,24,24,24,25,25,26,26,27,27,28,29,29,30,31,31,32,33,33,34,35,36,37,38,38,39,40,42,43,43,44,45,46,47,48,49,50,51,52,53,54,55,55,56,58,59,60,61,62,63,64,65,66,67,68,69,70,72,73,74,75)

/**
 * o2 max 电池数组
 */
val oxyBatArr = arrayListOf<Int>(0, 0, 0, 0, 0, 0, 0, 5, 5, 6, 7, 7, 8, 9, 10, 11, 12, 12, 13, 13, 14, 15, 16, 16, 17, 18, 19, 20, 21, 22, 23, 24, 24, 25, 26, 26, 27, 27, 29, 29, 31, 32, 33, 34, 35, 36, 36, 37, 37, 38, 39, 39, 41, 42, 42, 43, 44, 45, 46, 47, 48, 49, 49, 0, 50, 52, 54, 54, 0, 55, 56, 58, 59, 59, 60, 60, 61, 62, 62, 63, 64, 65, 65, 66, 68, 69, 69, 70, 70, 71, 72, 72, 73, 73, 74, 75, 77, 79, 79, 80, 80)

/**
 * kca 电池数组
 */
val kcaBatArr = arrayListOf<Int>(0, 0, 0, 0, 0, 0, 10, 19, 28, 35, 42, 48, 54, 59, 63, 68, 72, 76, 79, 83, 86, 89, 92, 95, 97, 100, 103, 105, 107, 109, 112, 114, 116, 118, 120, 121, 123, 125, 127, 128, 130, 131, 133, 134, 136, 137, 139, 140, 141, 143, 144, 145, 146, 148, 149, 150, 151, 152, 153, 154, 155, 157, 158, 159, 160, 161, 162, 162, 163, 164, 165, 166, 167, 168, 169, 170, 170, 171, 172, 173, 174, 175, 175, 176, 177, 178, 178, 179, 180, 180, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188)

var hostIp: String? = null
var hostPort: Int? = null

var hostNeedConnect : Boolean = false

/**
 * phone
 */

var id : String? = null

// wifi
var wifiState = false
// -45 ~ -100, 加上100
var wifiRssi = 100
var wifiSsid = ""
//var hostState = false

var bleRssi = -100

//var socketState = false
var socketToken: ByteArray? = null

fun clearSocketVars() {
//    socketState = false
    socketToken = null
}

var networkErrors = 0
var longitude = 0
var latitude = 0