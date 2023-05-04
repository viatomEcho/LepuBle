package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.EcnBleResponse
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.demo.data.EcnData

class EcnViewModel :InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnGetFileList)
            .observe(owner) {
                val data = it.data as EcnBleResponse.FileList
                val names = arrayListOf<String>()
                for (file in data.list) {
                    names.add(file.fileName)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileComplete)
            .observe(owner) { event ->
                val data = event.data as EcnBleResponse.File
                val temp = EcnData()
                temp.fileName = data.fileName
                temp.data = data.content
                _ecnData.value = temp
                _readNextFile.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.ECN.EventEcnReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
    }

}