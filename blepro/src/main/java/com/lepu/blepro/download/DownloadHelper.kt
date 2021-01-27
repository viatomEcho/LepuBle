package com.lepu.blepro.download

import android.text.TextUtils
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.LepuBleLog
import java.io.File
import java.io.RandomAccessFile

/**
 * author: wujuan
 * created on: 2020/12/14 14:20
 * description:
 */
class DownloadHelper {


    companion object{
        private const val tag: String = "DownloadHelper"

        fun writeFile(model: Int, userId: String, fileName: String, data: ByteArray){

            val folder = BleServiceHelper.BleServiceHelper.rawFolder?.get(model)

            LepuBleLog.d(tag, "$folder, $fileName, userId=$userId")
            if (TextUtils.isEmpty(folder)){
                LepuBleLog.d(tag, "保存路径有误")
                return
            }

            val mFile: File? =
                when(model){
                    Bluetooth.MODEL_O2RING -> {
                        File(folder, "$userId$fileName.dat")
                    }
                    Bluetooth.MODEL_ER1 -> {
                        File(folder, "$userId$fileName.dat")
                    }
                    else -> null
                }

            LepuBleLog.d("raw 文件 model=$model", if (mFile?.exists()!!) "存在" else "不存在")

            if (!mFile.exists()) {
                mFile.parentFile.mkdirs()
                mFile.createNewFile()
            }
            var randomFile: RandomAccessFile?
            randomFile = RandomAccessFile(mFile, "rw")
            val fileLength = randomFile.length()
            randomFile.seek(fileLength)
            randomFile.write(data)
            randomFile.close()
        }


    }
}