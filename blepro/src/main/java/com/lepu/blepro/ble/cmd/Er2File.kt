package com.lepu.blepro.ble.cmd

import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/2/26 16:15
 * description:
 */
class Er2File(val model: Int, val name: String, val size: Int, private val userId: String) {
    var fileName: String
    var fileSize: Int
    var content: ByteArray
    var index: Int // 标识当前下载index

    init {
        fileName = name
        fileSize = size
        content = ByteArray(size)
        index = 0
    }

    fun addContent(bytes: ByteArray) {
        if (index >= fileSize) {
            return // 已下载完成
        } else {
            System.arraycopy(bytes, 0, content, index, bytes.size)
//            DownloadHelper.writeFile(model, userId, fileName, "dat", bytes) //此设备不保存到本地

            index += bytes.size
        }
        LepuBleLog.d("Er2File,bytes size = ${bytes.size}, index = $index")
    }
}