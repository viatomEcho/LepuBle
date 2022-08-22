package com.lepu.blepro.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

public fun makeTimeStr(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

fun isNumber(name: String): Boolean {
    val str = "[0-9]+"
    val p = Pattern.compile(str)
    val m = p.matcher(name)
    return m.matches()
}

fun getTimeString(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): String {
    val monthStr = if (month < 10) {
        "0$month"
    } else {
        "$month"
    }
    val dayStr = if (day < 10) {
        "0$day"
    } else {
        "$day"
    }
    val hourStr = if (hour < 10) {
        "0$hour"
    } else {
        "$hour"
    }
    val minuteStr = if (minute < 10) {
        "0$minute"
    } else {
        "$minute"
    }
    val secondStr = if (second < 10) {
        "0$second"
    } else {
        "$second"
    }
    return "$year$monthStr$dayStr$hourStr$minuteStr$secondStr"
}