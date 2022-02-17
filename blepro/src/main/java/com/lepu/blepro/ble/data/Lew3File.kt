package com.lepu.blepro.ble.data

import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.utils.LepuBleLog

/**
 * author: wujuan
 * created on: 2021/2/26 16:15
 * description:
 */
class Lew3File(val model: Int, val name: String, val size: Int) {
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
        LepuBleLog.d("LeW3File,bytes size = ${bytes.size}, index = $index")
    }
}