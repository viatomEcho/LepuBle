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

    interface Download{
        companion object{
            const val EventIsPaused = "com.lepu.ble.download.paused"
            const val EventIsContinue = "com.lepu.ble.download.continue"
            const val EventIsCancel = "com.lepu.ble.download.cancel"
        }
    }

    interface Updater{
        companion object{
            const val EventEr1BleConnected = "com.lepu.ble.updater.ble.connected"
        }
    }


}