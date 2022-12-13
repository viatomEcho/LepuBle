package com.lepu.blepro.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.base.MANAGER_TAG
import com.lepu.blepro.utils.LepuBleLog
import no.nordicsemi.android.ble.RequestQueue
import java.util.*

class Vtm20fBleManager(context: Context): LpBleManager(context) {
    override fun initUUID() {
        service_uuid = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FFE4-0000-1000-8000-00805F9B34FB")
        LepuBleLog.d("Vtm20fBleManager initUUID")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        LepuBleLog.d("Vtm20fBleManager dealReqQueue")
        return requestQueue
    }

    override fun initialize() {
        LepuBleLog.d("Vtm20fBleManager initialize")
    }

    override fun getGattCallback(): BleManagerGattCallback {

        return object : BleManagerGattCallback(){
            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                LepuBleLog.d(MANAGER_TAG, "service_uuid = $service_uuid,write_uuid = $write_uuid,notify_uuid = $notify_uuid,isUpdater = $isUpdater")

                val service = gatt.getService(service_uuid)
                LepuBleLog.d(MANAGER_TAG, "service ==  $service")

                service?.let {
                    write_char = service.getCharacteristic(write_uuid)
                    notify_char = service.getCharacteristic(notify_uuid)

                    LepuBleLog.d(MANAGER_TAG, "writeChar ==  $write_char")
                    LepuBleLog.d(MANAGER_TAG, "notifyChar ==  $notify_char")
                }?: kotlin.run {
                    if (isUpdater) return true
                }

                var notify = false
                notify_char?.let{

                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "notifyChar properties ==  $properties")

                    notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                    LepuBleLog.d(MANAGER_TAG, "notifyChar notify ==  $notify")
                }

                var writeRequest = false
                write_char?.let {
                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "writeChar properties ==  $properties")

                    var writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                        writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    }
                    it.writeType = writeType

                    writeRequest =
                        properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 || properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                    LepuBleLog.d(MANAGER_TAG, "writeChar writeRequest ==  $writeRequest")
                }
                // Return true if all required services have been found
                return notify_char != null && notify
            }

            override fun initialize() {
                super.initialize()
                LepuBleLog.d(MANAGER_TAG, "initialize")
                buildRequestQueue()
                setNotify()
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int) {
                super.onMtuChanged(gatt, mtu)
                log(Log.INFO, "onMtuChanged mtu == $mtu")
            }

            override fun onDeviceDisconnected() {
                write_char = null
                notify_char = null
            }
        }
    }

}