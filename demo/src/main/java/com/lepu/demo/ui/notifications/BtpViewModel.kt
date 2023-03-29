package com.lepu.demo.ui.notifications

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.BtpBleResponse
import com.lepu.blepro.event.InterfaceEvent

class BtpViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpGetFileList)
            .observe(owner) {
                val data = it.data as BtpBleResponse.FileList
                val names = arrayListOf<String>()
                for (file in data.fileNames) {
                    names.add(file)
                }
                _fileNames.value = names
                _info.value = names.toString()
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadingFileProgress)
            .observe(owner) { event ->
                (event.data as Int).let {
                    _process.value = it
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadFileComplete)
            .observe(owner) { event ->
//                (event.data as BtpBleResponse.BtpFile).let {
                _readNextFile.value = true
//                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpReset)
            .observe(owner) {
                _reset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryReset)
            .observe(owner) {
                _factoryReset.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.BTP.EventBtpFactoryResetAll)
            .observe(owner) {
                _factoryResetAll.value = true
            }
    }

}