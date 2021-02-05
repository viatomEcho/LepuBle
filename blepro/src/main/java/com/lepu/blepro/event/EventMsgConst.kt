package com.lepu.blepro.event

/**
 * 公共的通知
 */
object EventMsgConst {


    /**
     * ble discovery
     */
    interface Discovery{
        companion object{
            const val EventDeviceFound = "com.lepu.ble.device.found"
            const val EventDeviceFound_Device = "com.lepu.ble.device.found.device"
            const val EventDeviceFound_ScanRecord = "com.lepu.ble.device.found.scanResult"
        }

    }

    /**
     * ble realtime task
     * 发送通知携带model
     */
    interface RealTime{
        companion object{
            const val EventRealTimeStart = "com.lepu.ble.realtime.start"
            const val EventRealTimeStop = "com.lepu.ble.realtime.stop"
        }
    }


    const val EventDeviceDisconnect = "com.lepu.ble.device.disconnect"


//    /**
//     * ER1 LiveDataBus Event
//     */
//    interface ER1{
//        companion object{
//            const val EventEr1Info = "com.lepu.ble.er1.info"
//            const val EventEr1RtData = "com.lepu.ble.er1.rtData"
//            const val EventEr1InvalidRtData = "com.lepu.ble.er1.invalid.rtData"
//            const val EventEr1Unbind = "com.lepu.ble.er1.unbind"
//        }
//    }




    /**
     * Oxy LiveDataBus Event
     */
//    interface Oxy{
//        companion object{
//            const val EventOxyReadFileError = "com.lepu.ble.oxy.read.file.error"
//            const val EventOxyReadFileComplete = "com.lepu.ble.oxy.read.file.complete"
//            const val EventOxyReadingFileProgress = "com.lepu.ble.oxy.reading.file.progress" // 当前文件进度
//
//            const val EventOxyResetDeviceInfo = "com.lepu.ble.oxy.reset"
//            const val EventOxySyncDeviceInfo = "com.lepu.ble.oxy.sync"
//
//            const val EventOxyInfo = "com.lepu.ble.oxy.info"
//
//            const val EventOxyRtData = "com.lepu.ble.oxy.rtData"
//        }
//    }


    /**
     * KcaBle LiveDataBus event
     */
    const val EventKcaSn = "com.lepu.ble.kac.sn"
    const val EventKcaMeasureState = "com.lepu.ble.kac.measure.state"
    const val EventKcaBpResult = "com.lepu.ble.kac.bp.result"
    const val EventKcaUnbind = "com.lepu.ble.kac.unbind"


    /**
     * bind new device LiveDataBus
     */
    const val EventBindEr1Device = "com.lepu.ble.bind.device.er1"
    const val EventBindO2Device = "com.lepu.ble.bind.device.o2"
    const val EventBindKcaDevice = "com.lepu.ble.bind.device.kca"


    /**
     * socket
     */
    const val EventSocketConnect = "com.lepu.socket.connect"
    const val EventSocketMsg = "com.lepu.socket.msg"
}