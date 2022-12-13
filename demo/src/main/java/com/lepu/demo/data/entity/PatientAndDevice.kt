package com.lepu.demo.data.entity

import com.google.gson.GsonBuilder
import com.lepu.demo.util.ext.typeToJson

class PatientAndDevice(
    var patientEntity: PatientEntity,
    var bleDeviceList:List<BleDevice>,
    var startTime: Long,
    var endTime: Long,
    var appVersion: String,
)

class BleDevice(
    var name: String,
    var sn: String
)

class SmartEcg(
   var deviceId: String,

)

fun PatientAndDevice.toJson(): String =
    GsonBuilder().create().typeToJson(this)
