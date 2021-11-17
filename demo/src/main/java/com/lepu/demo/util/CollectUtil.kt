package com.lepu.demo.util

import android.content.Context
import android.util.Log
import android.util.SparseArray
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.BuildConfig
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.O2RING_MODEL
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.PATIENT_DEVICE_JSON
import com.lepu.demo.data.entity.BleDevice
import com.lepu.demo.data.entity.PatientAndDevice
import com.lepu.demo.data.entity.PatientEntity
import com.lepu.demo.data.entity.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.*


/**
 * author: wujuan
 * description:
 */


class CollectUtil private constructor(val context: Context) {
    val TAG: String = "CollectUtil"

    companion object : SingletonHolder<CollectUtil, Context>(::CollectUtil)


    var o2ringCrtData: ByteArray = ByteArray(0)

    /**
     * 采集开始时间
     */
    var collectStartTime: Long = 0L
    var collectEndTime: Long = 0L

    /**
     * 是否添加数据中
     */
    var isTasking: Boolean = false

    var isSaving: Boolean = false

    /**
     * 实时数据本地文件路径
     */
    var rtExportPath: SparseArray<String> = SparseArray()

    val DEFAULT_DURATION: Long = 180// 默认3分钟

    val LEAST_DURATION: Long = 60 //定时结束采集设置的最短时长
    val MAX_DURATION: Long = 1800 //定时结束采集设置的最长时长

    var currentCollectDuration: Long = DEFAULT_DURATION //当前的采集时长



    fun initCurrentCollectDuration(){
        PrefUtils.readLongPreferences(context, PrefUtils.COLLECT_DURATION_SETTING).let {
            setCollectDurationAndType(if (it == 0L) currentCollectDuration else it)
        }
    }


    /**
     * 设置时长和采集类型
     */
    fun setCollectDurationAndType(duration: Long){

        currentCollectDuration = if (duration in LEAST_DURATION..MAX_DURATION) duration else if (duration <= LEAST_DURATION ) LEAST_DURATION else MAX_DURATION//设置当前采集时长
        PrefUtils.savePreferences(context, PrefUtils.COLLECT_DURATION_SETTING, currentCollectDuration)


        LepuBleLog.d("collect setting: duration = $currentCollectDuration")

    }

   @Synchronized fun collectO2RtData(rtData: ByteArray) {
        if (!isTasking || isSaving) return
        o2ringCrtData = addByteArrayData(o2ringCrtData, rtData)
        LepuBleLog.d(TAG, "collectO2RtData size =  ${o2ringCrtData.size}")
    }

    @Synchronized private fun addByteArrayData(oldData: ByteArray, feed: ByteArray): ByteArray {

        return ByteArray(oldData.size + feed.size).apply {
            oldData.copyInto(this)
            feed.copyInto(this, oldData.size)
        }
    }

    /**
     * 开启采集(血压开始)
     */
    fun startCollectData() {
        isTasking = true
        collectStartTime = System.currentTimeMillis()
        LepuBleLog.d(TAG, "采集开始了 isCollect = $isTasking, startTime = $collectStartTime")
    }



    /**
     * 中断
     */
    fun breakCollect() {
        isTasking = false
        LepuBleLog.d(TAG, "采集中断")
        releaseAll(true)

    }


    /**
     * 将实时数据数组写入文件
     */
    suspend fun saveLocalData(
        model: Int,
        patientEntity: PatientEntity,
        o2ringName: String

    ): kotlinx.coroutines.flow.Flow<Boolean> {
        return flow<Boolean> {

            try {
                collectEndTime = System.currentTimeMillis()

                LepuBleLog.d(
                    TAG,
                    "采集完成去保存...startTime = ${collectStartTime}, endTime = $collectEndTime, ${o2ringCrtData.size} "
                )


                if (o2ringCrtData.isEmpty()) {
                    LepuBleLog.d(
                        TAG,
                        "采集数据有误 无法保存 ${o2ringCrtData.size}"
                    )
                    emit(false)

                }

                if (!initExportFilePath(model)) emit(false)


                // 保存蓝牙设备数据

                FileUtil.saveFile(rtExportPath.get(O2RING_MODEL), o2ringCrtData, false)


                //保存患者信息及设备信息
                buildJson(patientEntity, o2ringName)?.let {
                    savePatientAndDeviceInfo(it)
                }



                emit(true)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                emit(false)
            } finally {
                isSaving = false
            }


        }.flowOn(Dispatchers.IO)
    }

    fun buildJson(
        patientEntity: PatientEntity,
        o2ringName: String,
    ): PatientAndDevice? {

        if (collectStartTime == 0L || collectEndTime == 0L) {

            LepuBleLog.e(TAG, "参数有误")
            return null
        }

        return PatientAndDevice(
            patientEntity,
            arrayListOf<BleDevice>(
                BleDevice(
                    "o2ring",
                    o2ringName
                ),
            ),
            collectStartTime,
            collectEndTime,
            BuildConfig.VERSION_NAME
        )

    }

    fun savePatientAndDeviceInfo(patientAndDevice: PatientAndDevice) {
        patientAndDevice.toJson().let {
            LepuBleLog.d(TAG, "patientAndDevice Json::: $it")

            FileUtil.saveTextFile(rtExportPath.get(PATIENT_DEVICE_JSON), it, false)
            LepuBleLog.d(TAG, "患者信息及设备信息已写入文件")

        }

    }


    /**
     * 初始化本地文件地址
     */
    fun initExportFilePath(model: Int): Boolean {
        if (collectStartTime == 0L) {
            LepuBleLog.d(TAG, "采集时间有误")
            return false
        }
        try {
            val startStr = DateUtil.stringFromDate(Date(collectStartTime), "yyyyMMddHHmmss")
            val endStr = DateUtil.stringFromDate(Date(collectEndTime), "yyyyMMddHHmmss")

            //创建文件夹
            val folder = "${SdLocal.getExportDataFolder(context)}/$startStr/"

            val pdJsonPath = "${folder}info.json"
            if (FileUtil.createParentDirFile("$pdJsonPath")) {
                LepuBleLog.d(TAG, "原始数据文件夹创建成功 $pdJsonPath")
            }

            rtExportPath.put(PATIENT_DEVICE_JSON, pdJsonPath)
            val filename = when (model) {
                O2RING_MODEL -> "o2ring.dat"
                else -> ""
            }
            "${folder}$filename".let { p ->
                LepuBleLog.d(TAG, p)
                rtExportPath.put(model, p)
            }

        } catch (e: Exception) {
            LepuBleLog.e(TAG, e.toString())
            e.printStackTrace()
            return false
        }

        return true


    }


    fun releaseAll(isBreak: Boolean) {
        Log.d(TAG, "releaseAll, isBreak = $isBreak")

        o2ringCrtData = ByteArray(0)

        if (isBreak) {
            //删除文件
            rtExportPath[O2RING_MODEL]?.let {
                FileUtil.deleteFile(context, File(it))
            }
        }
        isSaving = false
        collectStartTime = 0L
        collectEndTime = 0L

        rtExportPath = SparseArray()
        isTasking = false
    }


    /**
     *
     * 自动采集
     * @return Flow<LpResult<Int>>
     */
    fun autoCountDown(): Flow<LpResult<Long>> {
        return  flow {
            try {
                for (index in currentCollectDuration -1L downTo 0L){
                    delay(1000)
                    LepuBleLog.d("autoCountDown 采集： $currentCollectDuration, index= $index")
                    if(!this@CollectUtil.isTasking){
                        //结束
                        LepuBleLog.d("autoCountDown 采集异常终止： $index")
                        return@flow
                    }
                    emit(LpResult.Success(index))


                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit(LpResult.Failure(e.cause))
            }
        }.flowOn(Dispatchers.IO)

    }


}


