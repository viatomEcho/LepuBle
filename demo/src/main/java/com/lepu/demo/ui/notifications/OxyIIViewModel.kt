package com.lepu.demo.ui.notifications

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.BleServiceHelper
import com.lepu.blepro.ble.cmd.OxyIIBleCmd
import com.lepu.blepro.ble.cmd.OxyIIBleResponse
import com.lepu.blepro.ble.data.OxyIIBleFile
import com.lepu.blepro.ble.data.PpgFile
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.data.OxyData
import java.io.File
import java.util.*

class OxyIIViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIGetFileList)
            .observe(owner) { event ->
                (event.data as OxyIIBleResponse.FileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNames) {
                        if (fileName.isNotEmpty()) {
                            names.add(fileName)
                        }
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReadFileComplete)
            .observe(owner) { event ->
                (event.data as OxyIIBleResponse.BleFile).let {
                    if (it.fileType == OxyIIBleCmd.FileType.OXY) {
                        val data = OxyIIBleFile(it.content)
                        val fileName = DateUtil.stringFromDate(Date(data.startTime*1000), "yyyyMMddHHmmss")
                        val temp = OxyData()
                        temp.fileName = fileName
                        temp.recordingTime = data.interval*data.size
                        temp.avgSpo2 = data.avgSpo2
                        temp.avgHr = data.avgHr
                        temp.minSpo2 = data.minSpo2
                        temp.dropsTimes3Percent = data.dropsTimes3Percent
                        temp.dropsTimes4Percent = data.dropsTimes4Percent
                        temp.asleepTimePercent = data.percentLessThan90
                        temp.durationTime90Percent = data.durationTime90Percent
                        temp.dropsTimes90Percent = data.dropsTimes90Percent
                        temp.asleepTime = data.asleepTime
                        temp.o2Score = data.o2Score
                        temp.startTime = data.startTime
                        val len = data.spo2List.size
                        val spo2s = IntArray(len)
                        val hrs = IntArray(len)
                        val motions = IntArray(len)
                        val warningSpo2s = BooleanArray(len)
                        val warningHrs = BooleanArray(len)
                        for (i in 0 until len) {
                            spo2s[i] = data.spo2List[i]
                            hrs[i] = data.prList[i]
                            motions[i] = data.motionList[i]
                            warningSpo2s[i] = data.remindsSpo2[i]
                            warningHrs[i] = data.remindHrs[i]
                        }
                        temp.spo2s = spo2s
                        temp.hrs = hrs
                        temp.motions = motions
                        temp.warningSpo2s = warningSpo2s
                        temp.warningHrs = warningHrs
                        _oxyData.value = temp
                    } else {
                        val data = PpgFile(it.content)
                        _info.value = _info.value + "\n$data"
                    }
                    _readNextFile.value = true
                    Log.d("111111111111", "_info.value : ${_info.value}")
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIReset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.OxyII.EventOxyIIFactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}