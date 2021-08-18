package com.lepu.blepro.event

import com.jeremyliao.liveeventbus.core.LiveEvent

/**
 * author: wujuan
 * created on: 2021/2/3 18:35
 * description: 从interface发送的业务通知，都要携带model。App使用时通过model区分同功能系列的不同设备
 */
class InterfaceEvent(val model: Int, val data: Any): LiveEvent {

    /**
     * Oxy LiveDataBus Event
     * OxyBleInterface发出的通知
     * 包含model: model_o2ring
     */
    interface Oxy{
        companion object{
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress" // 当前文件进度 展示时：(dialogProgress / 10.0) + "%")

            const val EventOxyResetDeviceInfo = "com.lepu.ble.oxy.reset"
            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"

            const val EventOxyInfo = "com.lepu.ble.oxy.info"
            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"
            const val EventOxyRtParamData = "com.lepu.ble.oxy.rt.param.Data"
            const val EventOxyPpgData = "com.lepu.ble.oxy.ppg.data"
            const val EventOxyPpgRes = "com.lepu.ble.oxy.ppg.res"
        }
    }

    /**
     * Er1BleInterface发出的通知
     * 包含model: model_er1\model_duoek\model_er2
     */
    interface ER1{
        companion object{
            const val EventEr1Info = "com.lepu.ble.er1.info"
            const val EventEr1RtData = "com.lepu.ble.er1.rtData"
            const val EventEr1FileList = "com.lepu.ble.er1.fileList"
            const val EventEr1ReadFileError = "com.lepu.ble.er1.read.file.error"
            const val EventEr1ReadingFileProgress = "com.lepu.ble.er1.reading.file.progress"
            const val EventEr1ReadFileComplete = "com.lepu.ble.er1.read.file.complete"
            const val EventEr1ReadFileCanceled = "com.lepu.ble.er1.read.file.canceled"
            const val EventEr1ResetFactory = "com.lepu.ble.er1.reset"
            const val EventEr1ResetFactoryAll = "com.lepu.ble.er1.reset.all"
            const val EventEr1VibrateConfig = "com.lepu.ble.er1.vibrate.config"
            const val EventEr1SetSwitcherState = "com.lepu.ble.er1.set.switcher.state"
            const val EventEr1SetTime = "com.lepu.ble.er1.set.time"


        }
    }

    /**
     * BpmBleInterface发出的通知
     * 包含model: model_bpm
     */
    interface BPM{
        companion object{
            const val EventBpmInfo = "com.lepu.ble.bpm.info"
            const val EventBpmRtData = "com.lepu.ble.bpm.rtData"
            const val EventBpmState = "com.lepu.ble.bpm.state"
            const val EventBpmFileList = "com.lepu.ble.bpm.fileList"
            const val EventBpmReadFileError = "com.lepu.ble.bpm.read.file.error"
            const val EventBpmReadingFileProgress = "com.lepu.ble.bpm.reading.file.progress"
            const val EventBpmReadFileComplete = "com.lepu.ble.bpm.read.file.complete"
            const val EventBpmResetDeviceInfo = "com.lepu.ble.bpm.reset"
            const val EventBpmSyncTime = "com.lepu.ble.bpm.sync.time"
            const val EventBpmRecordData = "com.lepu.ble.bpm.record.data"
            const val EventBpmRecordEnd = "com.lepu.ble.bpm.record.end"
            const val EventBpmMeasureResult = "com.lepu.ble.bpm.measure.result"

        }
    }
    /**
     * BpmBleInterface发出的通知
     * 包含model: model_bpm
     */
    interface BP2{
        companion object{
            const val EventBp2Info = "com.lepu.ble.bp2.info"
            const val EventBp2RtData = "com.lepu.ble.bp2.rtData"
            const val EventBp2State = "com.lepu.ble.bp2.state"
            const val EventBp2FileList = "com.lepu.ble.bp2.fileList"
            const val EventBp2ReadFileError = "com.lepu.ble.bp2.read.file.error"
            const val EventBp2ReadingFileProgress = "com.lepu.ble.bp2.reading.file.progress"
            const val EventBp2ReadFileComplete = "com.lepu.ble.bp2.read.file.complete"
            const val EventBp2ResetDeviceInfo = "com.lepu.ble.bp2.reset"
            const val EventBp2SyncTime = "com.lepu.ble.bp2.sync.time"
            const val EventBp2RecordData = "com.lepu.ble.bp2.record.data"
            const val EventBp2RecordEnd = "com.lepu.ble.bp2.record.end"
            const val EventBp2MeasureResult = "com.lepu.ble.bp2.measure.result"

            const val EventBpSetConfigResult = "com.lepu.ble.bp2.measure.config"
            const val EventBpGetConfigResult = "com.lepu.ble.bp2.measure.getConfig"
            const val EventBpSwitchState = "com.lepu.ble.bp2.switch.state"

        }
    }
    /**
     * BpmBleInterface发出的通知
     * 包含model: model_bpm
     */
    interface ER2{
        companion object{
            const val EventEr2Info = "com.lepu.ble.er2.info"
            const val EventEr2SetTime = "com.lepu.ble.er2.set.time"
            const val EventEr2SetSwitcherState = "com.lepu.ble.er2.set.switcher.state"
            const val EventEr2SwitcherState = "com.lepu.ble.er2.switcher.state"
            const val EventEr2FactoryReset = "com.lepu.ble.er2.factory.reset"
            const val EventEr2RtData = "com.lepu.ble.er2.realtime.data"
            const val EventEr2FileList = "com.lepu.ble.er2.file.list"
            const val EventEr2ReadFileError = "com.lepu.ble.er2.file.read.error"
            const val EventEr2ReadingFileProgress = "com.lepu.ble.er2.file.reading.progress"
            const val EventEr2ReadFileComplete = "com.lepu.ble.er2.file.read.complete"

        }
    }


    interface PC60Fw{
        companion object{
            const val EventPC60FwRtDataParam = "com.lepu.ble.pc60fw.rt.data.param"
            const val EventPC60FwRtDataWave = "com.lepu.ble.pc60fw.rt.data.wave"
            const val EventPC60FwBattery = "com.lepu.ble.pc60fw.battery"
        }
    }


}