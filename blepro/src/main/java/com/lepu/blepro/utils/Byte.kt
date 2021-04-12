package com.lepu.blepro.utils

fun toSignedShort(b1: Byte, b2: Byte): Int {
    return ((b1.toInt() and 0xff) + (b2.toInt() and 0x7f shl 8))
}
