package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.data.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.data.*
import com.lepu.demo.util.DataConvert

class Bp2ViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2Reset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2FileList)
            .observe(owner) { event ->
                (event.data as KtBleFileList).let {
                    val names = arrayListOf<String>()
                    for (fileName in it.fileNameList) {
                        names.add(fileName)
                    }
                    _fileNames.value = names
                    _info.value = names.toString()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Bp2FilePart).let {
                    _process.value = (it.percent.times(100)).toInt()
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BP2.EventBp2ReadFileComplete)
            .observe(owner) { event ->
                (event.data as Bp2BleFile).let {
                    val content = DownloadHelper.readFile(event.model, "", it.name)
                    val file = if (content.isEmpty()) {
                        it
                    } else {
                        Bp2BleFile(it.name, content, it.deviceName)
                    }
                    if (file.type == 2) {
                        val data = Bp2EcgFile(file.content)
                        val temp = getEcgData(data.measureTime.toLong(), it.name, data.waveData, DataConvert.getBp2ShortArray(data.waveData), data.recordingTime)
                        _ecgData.value = temp
                    } else if (file.type == 1) {
                        val data = Bp2BpFile(file.content)
                        val temp = BpData()
                        temp.fileName = it.name
                        temp.sys = data.sys
                        temp.dia = data.dia
                        temp.pr = data.pr
                        temp.mean = data.mean
                        _bpData.value = temp
                    }
                    _readNextFile.value = true
                }
            }
    }

}