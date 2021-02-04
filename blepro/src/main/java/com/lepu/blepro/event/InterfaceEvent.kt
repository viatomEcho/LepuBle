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
     */
    interface Oxy{
        companion object{
            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"
            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"
            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress" // 当前文件进度

            const val EventOxyResetDeviceInfo = "com.lepu.ble.oxy.reset"
            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"

            const val EventOxyInfo = "com.lepu.ble.oxy.info"
            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"
        }
    }

    interface ER1{
        companion object{
            const val EventEr1Info = "com.lepu.ble.er1.info"
            const val EventEr1RtData = "com.lepu.ble.er1.rtData"
            const val EventEr1FileList = "com.lepu.ble.er1.fileList"
            const val EventEr1ReadFileError = "com.lepu.ble.er1.read.file.error"
            const val EventEr1ReadingFileProgress = "com.lepu.ble.er1.reading.file.progress"
            const val EventEr1ReadFileComplete = "com.lepu.ble.er1.read.file.complete"
            const val EventEr1ResetDeviceInfo = "com.lepu.ble.er1.reset"
        }
    }

}