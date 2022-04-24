package com.lepu.demo

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.demo.ble.LpBleUtil
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.O2RING_MODEL
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.currentModel
import com.lepu.demo.cofig.Constant.BluetoothConfig.Companion.singleConnect
import com.lepu.demo.data.entity.DeviceEntity
import com.lepu.demo.data.entity.PatientEntity
import com.lepu.demo.data.entity.RecordEntity
import com.lepu.demo.local.DBHelper
import com.lepu.demo.util.CollectUtil
import com.lepu.demo.util.ToastUtil
import com.lepu.demo.util.doFailure
import com.lepu.demo.util.doSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * @ClassName MainViewModel
 * @Description TODO
 * @Author wujuan
 * @Date 2021/11/8 10:10
 */
class MainViewModel: ViewModel() {
    val tag = "MainViewModel"

    /**
     * 蓝牙可用状态
     */
    val _bleEnable = MutableLiveData<Boolean>().apply {
        value = false
    }
    val bleEnable: LiveData<Boolean> = _bleEnable


    /**
     * 是否扫描中
     */
    val _scanning = MutableLiveData<Boolean>().apply {
        value = false
    }
    val scanning: LiveData<Boolean> = _scanning
    
    val _bleState = MutableLiveData<Boolean>().apply {
        value = false
    }
    val bleState: LiveData<Boolean> = _bleState

    var runWave = false

    /**
     * 设备信息
     */
    // 心电产品
    val _er1Info = MutableLiveData<LepuDevice>()
    val er1Info: LiveData<LepuDevice> = _er1Info

    val _er2Info = MutableLiveData<Er2DeviceInfo>()
    val er2Info: LiveData<Er2DeviceInfo> = _er2Info

    val _pc80bInfo = MutableLiveData<PC80BleResponse.DeviceInfo>()
    val pc80bInfo: LiveData<PC80BleResponse.DeviceInfo> = _pc80bInfo

    val _bp2Info = MutableLiveData<Bp2DeviceInfo>()
    val bp2Info: LiveData<Bp2DeviceInfo> = _bp2Info

    val _bpmInfo = MutableLiveData<BpmDeviceInfo>()
    val bpmInfo: LiveData<BpmDeviceInfo> = _bpmInfo

    val _oxyInfo = MutableLiveData<OxyBleResponse.OxyInfo>()
    val oxyInfo: LiveData<OxyBleResponse.OxyInfo> = _oxyInfo

    val _pc100Info = MutableLiveData<Pc100DeviceInfo>()
    val pc100Info: LiveData<Pc100DeviceInfo> = _pc100Info

    val _boInfo = MutableLiveData<BoDeviceInfo>()
    val boInfo: LiveData<BoDeviceInfo> = _boInfo

    val _oxyPrAlarmFlag = MutableLiveData<Boolean>().apply { value = false }
    val oxyPrAlarmFlag: LiveData<Boolean> = _oxyPrAlarmFlag

    val _aoj20aInfo = MutableLiveData<Aoj20aBleResponse.DeviceData>()
    val aoj20aInfo: LiveData<Aoj20aBleResponse.DeviceData> = _aoj20aInfo

    val _checkmePodInfo = MutableLiveData<CheckmePodBleResponse.DeviceInfo>()
    val checkmePodInfo: LiveData<CheckmePodBleResponse.DeviceInfo> = _checkmePodInfo

    val _pulsebitInfo = MutableLiveData<PulsebitBleResponse.DeviceInfo>()
    val pulsebitInfo: LiveData<PulsebitBleResponse.DeviceInfo> = _pulsebitInfo

    val _checkmeLeInfo = MutableLiveData<CheckmeLeBleResponse.DeviceInfo>()
    val checkmeLeInfo: LiveData<CheckmeLeBleResponse.DeviceInfo> = _checkmeLeInfo

    val _pc300Info = MutableLiveData<Pc300DeviceInfo>()
    val pc300Info: LiveData<Pc300DeviceInfo> = _pc300Info

    /**
     * 当前蓝牙
     */
    val _curBluetooth = MutableLiveData<DeviceEntity?>()
    var curBluetooth: LiveData<DeviceEntity?> = _curBluetooth

    val _o2ringCurBluetooth = MutableLiveData<DeviceEntity?>()
    var o2ringCurBluetooth: LiveData<DeviceEntity?> = _o2ringCurBluetooth

    /**
     * 是否准备采集中（点击采集）
     */
    val _preingCollect = MutableLiveData<Boolean>().apply {
        value = false
    }
    val preingCollect: LiveData<Boolean> = _preingCollect


    /**
     * 当前采集用户
     */
    val _curPatient = MutableLiveData<PatientEntity?>().apply { value = null }
    var curPatient: LiveData<PatientEntity?> = _curPatient


    val _successShowing = MutableLiveData<Boolean>().apply { value = false }
    var successShowing: LiveData<Boolean> = _successShowing


    val _countDown = MutableLiveData<Long>()
    val countDown: LiveData<Long> = _countDown


    fun getCurPatient(context: Context) {
        viewModelScope.launch {
            DBHelper.getInstance(context).getCurrentPatient()
                .onStart { }
                .catch { }
                .collect { res ->
                    res.doSuccess {
                        _curPatient.value = it
                    }
                    res.doFailure {

                    }

                }
        }
    }
    @Synchronized
    fun collectO2ring(context: Context, data: ByteArray) {

        if (!_preingCollect.value!!) {
            LepuBleLog.e(tag, "采集未开启，不保存实时数据")
            return
        }

        CollectUtil.getInstance(context).collectO2RtData(data)
    }
    /**
     * 开启采集准备状态
     */
    fun startPreCollect(context: Context) {

        if (!collectEnvironmentOk(context, true)) {
            return
        }

        if (CollectUtil.getInstance(context).isTasking || CollectUtil.getInstance(context).isSaving) {
            LepuBleLog.d(tag, "上次采集过程还未结束, 无法采集")
            Toast.makeText(context, "上次采集过程还未结束，无法开始采集", Toast.LENGTH_SHORT).show()
            return
        }

        _preingCollect.value = true

        Toast.makeText(context, "开始采集", Toast.LENGTH_SHORT).show()


    }



    @Synchronized fun checkStartCollect(activity: Activity, wave: ByteArray?){
        if (preingCollect.value == true ){

            if (wave != null) {

                //可以采集数据了

                CollectUtil.getInstance(activity.applicationContext).let { util ->

                    if (!util.isTasking && !util.isSaving){
                        LepuBleLog.d(tag,"即将进入采集 isTasking = ${util.isTasking}, isSaving = ${util.isSaving}")
                        util.startCollectData()
                       startCountDown(activity.applicationContext)
                    }


                    if (util.isTasking) {
                        collectO2ring(activity.applicationContext, wave)
                    }
                }

            }else{
                ToastUtil.showToast(activity,"wave.isEmpty")
            }
        }else{
//            ToastUtil.showToast(activity,"preingCollect  false")
        }
    }

    fun startCountDown(context: Context){

        viewModelScope.launch {
            CollectUtil.getInstance(context).autoCountDown()
                .onStart {
                    LepuBleLog.d("collect countdown onStart")

                }
                .onCompletion {
                    LepuBleLog.d("collect countdown onCompletion")

                    _countDown.postValue(0L)
                }
                .catch {  }
                .collect { result ->
                    result.doFailure {
                        _countDown.postValue(0L)
                    }
                    result.doSuccess {
                        _countDown.postValue(it)
                        if(it == 0L){
                            if (_preingCollect.value == true && CollectUtil.getInstance(context).isTasking){
                                //采集完成 停止采集并保存数据
                                LepuBleLog.d("countdown  保存数据")
                                stopAndSave(context)
                            }
                        }

                    }
                }

        }

    }

    /**
     * 检查设备运行状态
     */
    fun collectEnvironmentOk(context: Context, isShowToast: Boolean): Boolean {
        if (_curPatient.value == null) {
            if (isShowToast) ToastUtil.showToast(context, "请先添加患者信息")
            return false
        }

        //检查连接状态 及实时状态
            currentModel.forEach {
                if (!LpBleUtil.isBleConnected(it)) {
                    if (isShowToast) ToastUtil.showToast(
                        context,
                        " ${if (it == O2RING_MODEL) "o2ring" else ""} 设备未连接，无法开始采集"
                    )
                    return false
                }

                if (LpBleUtil.isRtStop(it)) {
                    if (isShowToast) ToastUtil.showToast(
                        context,
                        "${if (it == O2RING_MODEL) "o2ring" else ""} 未开启实时，无法开始采集"
                    )
                    return false
                }
            }


        return true
    }


    /**
     * 中断采集
     */
    fun breakCollecting(context: Context) {
        _preingCollect.value = false
        CollectUtil.getInstance(context).let {
            it.breakCollect()
        }
    }

    /**
     * 正常采集完成并去保存
     */
    fun stopAndSave(context: Context) {
        _countDown.value = 0L


        LepuBleLog.e("stopAndSave", "start")

        _curPatient.value?.let { p ->
            CollectUtil.getInstance(context).let { collect ->
                if (!collect.isTasking || collect.isSaving) {
                    LepuBleLog.e("stopAndSave", "!it.isTasking || it.isSaving ")

                    breakCollecting(context)
                    return
                }

                if (_o2ringCurBluetooth.value == null) {
                    LepuBleLog.e( tag, "buildJson => o2ring == null")
                    breakCollecting(context)
                    return
                }

                viewModelScope.launch(Dispatchers.Main) {
                        if (singleConnect) {
                            collect.saveLocalData(
                                currentModel[0],
                                p,
                                _o2ringCurBluetooth.value!!.deviceName
                            )
                                .onStart {
                                    collect.isTasking = false
                                    collect.isSaving = true

                                    Toast.makeText(context, "正在保存数据...", Toast.LENGTH_SHORT).show()
                                }

                                .catch {
                                    breakCollecting(context)
                                }
                                .collect { result ->
                                    if (result)
                                        saveRtSucess(context, collect, p)
                                    else {
                                        breakCollecting(context)
                                        Toast.makeText(context, "数据保存失败", Toast.LENGTH_SHORT).show()
                                    }

                                }
                        }

                    }


            }
        } ?: kotlin.run {
            LepuBleLog.e("stopAndSave", "患者信息丢失")
        }


    }


    fun saveRtSucess(context: Context, collectUtil: CollectUtil, patientEntity: PatientEntity) {
        //保存流水
        GlobalScope.launch(Dispatchers.Main) {
            RecordEntity(
                startTime = collectUtil.collectStartTime,
                endTime = collectUtil.collectEndTime,
                name = patientEntity.name,
                age = patientEntity.age,
                gender = patientEntity.gender,
                patientId = patientEntity.patientId
            ).also { record ->
                DBHelper.getInstance(context).insertRecord(record)
                    .onCompletion {
                        collectUtil.releaseAll(false)
                        _preingCollect.value = false


                        //再次采集提示
                        _successShowing.value = true

                    }
                    .catch {

                    }
                    .collect {

                        it.doSuccess {
                            LepuBleLog.e("collect", "流水保存成功")
                            Toast.makeText(context, "流水保存成功", Toast.LENGTH_SHORT).show()
                        }
                        it.doFailure {
                            LepuBleLog.e("collect", "流水保存失败，忽略不处理")
                            Toast.makeText(context, "数据保存失败", Toast.LENGTH_SHORT).show()
                        }

                    }

            }

        }


    }


    /**
     * 响应点击采集
     */
    fun actionCollect(context: Context) {
        _preingCollect.value?.let {
            LepuBleLog.d("actionCollect", it.toString())


            if (it) {
                CollectUtil.getInstance(context).let { c ->
                    if (c.isSaving) {
                        ToastUtil.showToast(context, "数据保存中...")
                        return
                    } else {
                        breakCollecting(context)
                    }
                }

            } else
                startPreCollect(context)
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun playAlarm(context: Context){
        LepuBleLog.d("alarm...")
        context?.let {
            val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 2000L, 200L,  3000L), -1))

        }
    }
//
//    /**
//     * 获取当前设备，即所有设备
//     */
//    fun getCurrentDevices(application: Application) {
//        DBHelper.getInstance(application).let {
//            viewModelScope.launch {
//                it.getCurrentDevices()
//                    .onStart {
//                        Log.d(tag, "开始查询当前设备")
//                    }
//                    .catch {
//                        Log.d(tag, "查询当前设备出错")
//                    }
//                    .onCompletion {
//                        Log.d(tag, "查询当前设备结束")
//
//                    }
//                    .collect { result ->
//                        result.doFailure {
//                            Log.d(tag, "查询当前设备失败")
//                        }
//                        result.doSuccess { list ->
//                            Log.d(tag, "查询当前设备成功 size = $list")
//                            if (list == null) {
////                                    _o2ringCurBluetooth.value = null
////                                    _pc60CurBluetooth.value = null
////                                    _bp2CurBluetooth.value = null
//                            } else {
//                                for (d in list) {
//                                    when (d.modelNo) {
//                                        O2RING_MODEL -> _o2ringCurBluetooth.value = d
//                                    }
//                                }
//                            }
//
//                            reconnectOrScan()
//
//                        }
//
//                    }
//            }
//
//        }
//    }
//
//    /**
//     * 每model只保存最近连接的一台设备
//     */
//    fun saveDevice(application: Application, deviceEntity: DeviceEntity) {
//        DBHelper.getInstance(application).let {
//            viewModelScope.launch(Dispatchers.IO) {
//                Log.d(tag, "saveDevice")
//                it.insertOrUpdateDevice(deviceEntity)
//            }
//
//        }
//    }
//
//    fun deleteDevice(application: Application, deviceEntity: DeviceEntity) {
//        DBHelper.getInstance(application).let {
//            viewModelScope.launch(Dispatchers.IO) {
//                Log.d(tag, "deleteDevice")
//                it.deleteDevice(deviceEntity)
//            }
//
//        }
//    }


}