package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
import com.lepu.demo.data.AnalysisFile
import com.lepu.demo.util.DataConvert

class Er1ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1FileList)
            .observe(owner) { event ->
                (event.data as String).let { data ->
                    val names = arrayListOf<String>()
                    for (fileName in data.split(",")) {
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
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it / 10
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Er1BleResponse.Er1File).let {
                    if (event.model == Bluetooth.MODEL_ER1_N) {
                        val content = DownloadHelper.readFile(it.model, "", it.fileName)
                        val data = if (content.isEmpty()) {
                            VBeatHrFile(it.content)
                        } else {
                            VBeatHrFile(content)
                        }
//                        _info.value = "$data"
                        val analysisFile = AnalysisFile()
                        analysisFile.fileName = it.fileName
                        analysisFile.isMotion = false
                        var count = 0
                        var index = 0
                        for (result in data.hrList) {
                            if (index % 60 == 0) {
                                count = 0
                            }
                            if (result.motion >= 16) {
                                if (count == 3) {
                                    analysisFile.isMotion = true
                                    break
                                } else {
                                    count++
                                }
                            }
                            index++
                        }
                        _analysisFile.value = analysisFile
                        _ecgData.value = getEcgData(0, it.fileName, ByteArray(0), ShortArray(0), data.recordingTime)
                    } else {
                        if (it.fileName.contains("R")) {
                            val data = Er1EcgFile(DownloadHelper.readFile(it.model, "", it.fileName))
                            val recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                            _ecgData.value = getEcgData(recordingTime, it.fileName, data.waveData, DataConvert.getEr1ShortArray(data.waveData), data.recordingTime)
                        } else {
                            val data = Er2AnalysisFile(DownloadHelper.readFile(it.model, "", it.fileName))
//                            _info.value = "$data"
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
                    }
                    _readNextFile.value = true
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactory)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ER1.EventEr1ResetFactoryAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}