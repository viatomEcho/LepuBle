package com.lepu.blepro.event

/**
 * 公共的通知
 */
object EventMsgConst {

    interface Ble{
        companion object{
            const val EventServiceConnectedAndInterfaceInit = "com.lepu.ble.service.interface.init"  // 服务连接后初始化interface成功会发送 true
            const val EventBleDeviceReady = "com.lepu.ble.device.ready"  // 没有同步时间的设备连接成功后会发送 model
        }
    }


    /**
     * ble discovery
     */
    interface Discovery{
        companion object{
            const val EventDeviceFound = "com.lepu.ble.device.found"  // 扫描到sdk已有model设备会发送 Bluetooth
            const val EventDeviceFound_Device = "com.lepu.ble.device.found.device"  // 开始扫描设置需要配对的信息 Bluetooth/BluetoothDevice
            const val EventDeviceFound_ScanRecord = "com.lepu.ble.device.found.scanResult"  // 开始扫描设置需要配对的信息 ScanRecord
            const val EventDeviceFoundForUnRegister = "com.lepu.ble.device.found.unregister"  // 扫描到sdk没有model设备会发送 ScanResult
            const val EventDeviceFound_ScanRecordUnRegister = "com.lepu.ble.device.found.scanResult.unregister"
            const val EventDeviceFound_ER1_UPDATE = "com.lepu.ble.device.found.er1Update"  // 扫描到er1 updater设备会发送
        }

    }

    /**
     * ble realtime task
     * 发送通知携带model
     */
    interface RealTime{
        companion object{
            //实时波形
            const val EventRealTimeStart = "com.lepu.ble.realtime.start"  // 开启实时监测任务后会发送 model
            const val EventRealTimeStop = "com.lepu.ble.realtime.stop"  // 停止实时监测任务后会发送 model


            //bp2 获取实时状态
            const val EventRealTimeStateStart = "com.lepu.ble.realtime.state.start"
            const val EventRealTimeStateStop = "com.lepu.ble.realtime.state.stop"
        }
    }

    interface Download{
        companion object{
            const val EventIsPaused = "com.lepu.ble.download.paused"  // 暂停下载设备文件会发送
            const val EventIsContinue = "com.lepu.ble.download.continue"  // 停止下载设备文件会发送
            const val EventIsCancel = "com.lepu.ble.download.cancel"  // 取消下载设备文件会发送
        }
    }

    interface Updater{
        companion object{
            const val EventBleConnected = "com.lepu.ble.updater.ble.connected"  // 升级设备连接成功会发送
        }
    }

    interface Cmd{
        companion object{
            const val EventCmdResponseTimeOut = "com.lepu.ble.cmd.response.timeout"  // 指令响应超时会发送
            const val EventCmdResponseContent = "com.lepu.ble.cmd.response.content"  // 指令响应
        }
    }



}