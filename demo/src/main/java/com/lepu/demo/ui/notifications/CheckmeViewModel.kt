package com.lepu.demo.ui.notifications

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus
import com.lepu.blepro.ble.cmd.*
import com.lepu.blepro.download.DownloadHelper
import com.lepu.blepro.event.InterfaceEvent
import com.lepu.blepro.objs.Bluetooth
import com.lepu.blepro.utils.DateUtil

class CheckmeViewModel : InfoViewModel() {

    fun initEvent(owner: LifecycleOwner) {
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileList)
            .observe(owner) {
                val data = it.data as CheckmeBleResponse.ListContent
                when (data.type) {
                    CheckmeBleCmd.ListType.DLC_TYPE -> {
                        val list = CheckmeBleResponse.DlcList(data.content)
                        val names = arrayListOf<String>()
                        for (file in list.list) {
                            names.add(file.recordName)
                        }
                        _fileNames.value = names
                        _info.value = names.toString()
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.TEMP_TYPE -> {
                        val list = CheckmeBleResponse.TempList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.ECG_TYPE -> {
                        val list = CheckmeBleResponse.EcgList(data.content)
                        val names = arrayListOf<String>()
                        for (file in list.list) {
                            names.add(file.recordName)
                        }
                        _fileNames.value = names
                        _info.value = names.toString()
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.OXY_TYPE -> {
                        val list = CheckmeBleResponse.OxyList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.BPCAL_TYPE -> {
                        val list = CheckmeBleResponse.BpcalList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.GLU_TYPE -> {
                        val list = CheckmeBleResponse.GluList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.BP_TYPE -> {
                        val list = CheckmeBleResponse.BpList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.SLM_TYPE -> {
                        val list = CheckmeBleResponse.SlmList(data.content)
                        val names = arrayListOf<String>()
                        for (file in list.list) {
                            names.add(file.recordName)
                        }
                        _fileNames.value = names
                        _info.value = names.toString()
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.PED_TYPE -> {
                        val list = CheckmeBleResponse.PedList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                    CheckmeBleCmd.ListType.USER_TYPE -> {
                        val list = CheckmeBleResponse.UserList(data.content)
                        _info.value = "$list"
                        Log.d("11111111111111111", "$list")
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeGetFileListProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileComplete)
            .observe(owner) {
                val data = it.data as CheckmeBleResponse.FileContent
                when (data.type) {
                    CheckmeBleCmd.ListType.ECG_TYPE, CheckmeBleCmd.ListType.DLC_TYPE -> {
                        val file = CheckmeBleResponse.EcgFile(data.content)
                        val temp = getEcgData(DateUtil.getSecondTimestamp(data.fileName), data.fileName, file.waveData, file.waveDecompress, file.recordingTime)
                        _ecgData.value = temp
                        _readNextFile.value = true
//                        DownloadHelper.writeFile(Bluetooth.MODEL_ER1, "checkme", data.fileName, "dat", data.content)
                    }
                    CheckmeBleCmd.ListType.SLM_TYPE -> {
                        val file = CheckmeBleResponse.SlmFile(data.content)
                        _info.value = "$file"
                        _readNextFile.value = true
                        Log.d("11111111111111111", "$file")
                    }
                }
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadFileError)
            .observe(owner) {
                _readFileError.value = true
            }
        LiveEventBus.get<InterfaceEvent>(InterfaceEvent.Checkme.EventCheckmeReadingFileProgress)
            .observe(owner) {
                _process.value = it.data as Int
            }
    }

}