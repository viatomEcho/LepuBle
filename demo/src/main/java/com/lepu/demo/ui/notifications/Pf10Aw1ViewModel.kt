package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Pf10Aw1BleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.data.OxyData
import java.util.*

class Pf10Aw1ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1GetFileList)
            .observe(owner) { event ->
                (event.data as Pf10Aw1BleResponse.FileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNames) {
                        if (fileName.isNotEmpty()) {
                            names.add(fileName)
                        }
                    }
                    names.sort()
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Pf10Aw1BleResponse.BleFile).let {
                    val fileName = DateUtil.stringFromDate(Date(it.startTime*1000), "yyyyMMddHHmmss")
                    val temp = OxyData()
                    temp.fileName = fileName
                    temp.recordingTime = it.interval*it.size
                    temp.startTime = it.startTime
                    val len = it.spo2List.size
                    val spo2s = IntArray(len)
                    val hrs = IntArray(len)
                    val motions = IntArray(len)
                    val warningSpo2s = BooleanArray(len)
                    val warningHrs = BooleanArray(len)
                    for (i in 0 until len) {
                        spo2s[i] = it.spo2List[i]
                        hrs[i] = it.prList[i]
                        motions[i] = 0
                        warningSpo2s[i] = false
                        warningHrs[i] = false
                    }
                    temp.spo2s = spo2s
                    temp.hrs = hrs
                    temp.motions = motions
                    temp.warningSpo2s = warningSpo2s
                    temp.warningHrs = warningHrs
                    _oxyData.value = temp
                    _readNextFile.value = true
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pf10Aw1.EventPf10Aw1FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}