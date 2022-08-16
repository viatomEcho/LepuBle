package com.lepu.blepro.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

public fun makeTimeStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun isNumber(name: String): Boolean {
    val pattern = Pattern.compile("[0-9]+")
    return pattern.matcher(name).matches()
}