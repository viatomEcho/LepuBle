package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.Er1BleResponse
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil
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
                        val data = VBeatHrFile(DownloadHelper.readFile(it.model, "", it.fileName))
//                        _info.value = "$data"
                    } else {
                        if (it.fileName.contains("R")) {
                            val data = Er1EcgFile(DownloadHelper.readFile(it.model, "", it.fileName))
                            val recordingTime = DateUtil.getSecondTimestamp(it.fileName.replace("R", ""))
                            _ecgData.value = getEcgData(recordingTime, it.fileName, data.waveData, DataConvert.getEr1ShortArray(data.waveData), data.recordingTime)
                        } else {
                            val data = Er2AnalysisFile(DownloadHelper.readFile(it.model, "", it.fileName))
//                            _info.value = "$data"
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