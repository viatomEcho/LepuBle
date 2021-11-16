package com.lepu.blepro.event

/**
 * 公共的通知
 */
object EventMsgConst {

    interface Ble{
        companion object{
            const val EventServiceConnectedAndInterfaceInit = "com.lepu.ble.service.interface.init"
        }
    }


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
            //实时波形
            const val EventRealTimeStart = "com.lepu.ble.realtime.start"
            const val EventRealTimeStop = "com.lepu.ble.realtime.stop"


            //bp2 获取实时状态
            const val EventRealTimeStateStart = "com.lepu.ble.realtime.state.start"
            const val EventRealTimeStateStop = "com.lepu.ble.realtime.state.stop"
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
            const val EventBleConnected = "com.lepu.ble.updater.ble.connected"
        }
    }

    interface Cmd{
        companion object{
            const val EventCmdResponseTimeOut = "com.lepu.ble.cmd.response.timeout"
        }
    }



}