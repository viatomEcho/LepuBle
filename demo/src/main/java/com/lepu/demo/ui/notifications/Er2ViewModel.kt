package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.data.AnalysisFile
import com.lepu.demo.util.DataConvert

class Er2ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FileList)
            .observe(owner) { event ->
                (event.data as Er2FileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNames) {
//                        if (fileName.contains("R")) {
                            if (fileName.isNotEmpty()) {
                                names.add(fileName)
                            }
//                        }
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it.div(10)
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Er2File).let {
                    if (it.fileName.contains("R")) {
                        val content = DownloadHelper.readFile(it.model, "", it.fileName)
                        val data = if (content.isEmpty()) {
                            Er1EcgFile(it.content)
                        } else {
                            Er1EcgFile(content)
                        }
                        val recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                        val temp = getEcgData(recordingTime, it.fileName, data.waveData, DataConvert.getEr1ShortArray(data.waveData), data.recordingTime)
                        _ecgData.value = temp
                    } else {
                        val content = DownloadHelper.readFile(it.model, "", it.fileName)
                        val data = if (content.isEmpty()) {
                            Er2AnalysisFile(it.content)
                        } else {
                            Er2AnalysisFile(content)
                        }
//                        _info.value = "$data"
                        val analysisFile = AnalysisFile()
                        analysisFile.fileName = it.fileName
                        analysisFile.isMotion = false
                        for (result in data.resultList) {
                            if (result.diagnosis.isMoving) {
                                analysisFile.isMotion = true
                                break
                            }
                        }
                        _analysisFile.value = analysisFile
                    }
                    _readNextFile.value = true
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER2.EventEr2ReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
    }

}