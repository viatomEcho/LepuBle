package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.PulsebitBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.utils.DateUtil

class PulsebitViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileList)
            .observe(owner) {
                val data = it.data as PulsebitBleResponse.FileList
                val names = arrayListOf<String>()
                for (file in data.list) {
                    names.add(file.recordName)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitGetFileListProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileComplete)
            .observe(owner) {
                val data = it.data as PulsebitBleResponse.EcgFile
                val temp = getEcgData(DateUtil.getSecondTimestamp(data.fileName), data.fileName, data.waveData, data.waveShortData, data.recordingTime)
                _ecgData.value = temp
                _readNextFile.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Pulsebit.EventPulsebitReadingFileProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
    }

}