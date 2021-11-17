package com.lepu.demo.util.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * author: wujuan
 * created on: 2021/7/15 13:31
 * description:
 */

fun <T> Gson.typeToJson(src: T): String = toJson(src)
inline fun <reified T : Any> Gson.fromJson(json: String): T = fromJson(json,
    object : TypeToken<T>() {}.type)