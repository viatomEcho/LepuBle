package com.lepu.demo.local

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.lepu.demo.data.entity.PatientEntity
import com.lepu.demo.util.ext.fromJson
import com.lepu.demo.util.ext.typeToJson
/**
 * author: wujuan
 * description:
 */
open class LocalTypeConverter {

    @TypeConverter
    fun json2PatientEntity(src: String): PatientEntity =
            GsonBuilder().create().fromJson(src)

    @TypeConverter
    fun PatientEntity2Json(data: PatientEntity): String =
            GsonBuilder().create().typeToJson(data)



    @TypeConverter
    fun json2ListString(src: String): List<String>? =
            GsonBuilder().create().fromJson(src)

    @TypeConverter
    fun listString2Json(data: List<String>?): String =
            GsonBuilder().create().typeToJson(data)




}