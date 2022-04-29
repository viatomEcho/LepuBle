package com.lepu.blepro.download

import android.text.TextUtils
import com.lepu.blepro.ext.BleServiceHelper
import com.lepu.blepro.utils.HexString
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

        /**
         * er1 er2  filename 需要去空格
         * @param model Int
         * @param userId String
         * @param fileName String
         * @param suffix String
         * @param data ByteArray
         */
        fun writeFile(model: Int, userId: String, fileName: String,suffix: String,  data: ByteArray){
            val trimStr = HexString.trimStr(fileName)
            val folder = BleServiceHelper.BleServiceHelper.rawFolder?.get(model)

            LepuBleLog.d(tag, "$folder, $trimStr, userId=$userId")
            if (TextUtils.isEmpty(folder)){
                LepuBleLog.d(tag, "保存路径有误")
                return
            }

            val mFile = File(folder, "$userId$trimStr.$suffix")

            LepuBleLog.d("raw 文件 model=$model, fileName = $fileName", if (mFile.exists()) "存在" else "不存在")

            if (!mFile.exists()) {
                mFile.parentFile.mkdirs()
                mFile.createNewFile()
            }
            var randomFile: RandomAccessFile?
            randomFile = RandomAccessFile(mFile, "rw")
            val fileLength = randomFile.length()
            LepuBleLog.d(tag, "已经存入的fileLength = $fileLength")
            randomFile.seek(fileLength)
            randomFile.write(data)
            randomFile.close()

        }


    }
}