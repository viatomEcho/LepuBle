package com.lepu.blepro.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.lepu.blepro.base.LpBleManager
import com.lepu.blepro.base.MANAGER_TAG
import com.lepu.blepro.ble.data.TmbInfo
import com.lepu.blepro.utils.HexString.trimStr
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import no.nordicsemi.android.ble.RequestQueue
import no.nordicsemi.android.ble.data.Data
import java.util.*

class TmbBleManager(context: Context): LpBleManager(context) {

    val tmbInfo = TmbInfo()

    lateinit var service_info_uuid: UUID
    lateinit var manufacturer_uuid: UUID
    lateinit var model_uuid: UUID
    lateinit var serial_uuid: UUID
    lateinit var hv_uuid: UUID
    lateinit var fv_uuid: UUID
    lateinit var sv_uuid: UUID
    lateinit var id_uuid: UUID
    var manufacturer_char: BluetoothGattCharacteristic? = null
    var model_char: BluetoothGattCharacteristic? = null
    var serial_char: BluetoothGattCharacteristic? = null
    var hv_char: BluetoothGattCharacteristic? = null
    var fv_char: BluetoothGattCharacteristic? = null
    var sv_char: BluetoothGattCharacteristic? = null
    var id_char: BluetoothGattCharacteristic? = null

    lateinit var write_uuid_ack: UUID
    lateinit var notify_uuid_ack: UUID
    var write_char_ack: BluetoothGattCharacteristic? = null
    var notify_char_ack: BluetoothGattCharacteristic? = null

    override fun initUUID() {
        service_info_uuid = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")
        manufacturer_uuid = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB")
        model_uuid = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")
        serial_uuid = UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB")
        hv_uuid = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB")
        fv_uuid = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")
        sv_uuid = UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB")
        id_uuid = UUID.fromString("00002A23-0000-1000-8000-00805F9B34FB")
        /*service_uuid = UUID.fromString("0000FF00-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000FF02-0000-1000-8000-00805F9B34FB")*/
        service_uuid = UUID.fromString("0000A610-0000-1000-8000-00805F9B34FB")
        write_uuid_ack = UUID.fromString("0000A622-0000-1000-8000-00805F9B34FB")
        write_uuid = UUID.fromString("0000A624-0000-1000-8000-00805F9B34FB")
        notify_uuid_ack = UUID.fromString("0000A625-0000-1000-8000-00805F9B34FB")
        notify_uuid = UUID.fromString("0000A621-0000-1000-8000-00805F9B34FB")
        indicate_uuid = UUID.fromString("0000A620-0000-1000-8000-00805F9B34FB")
        LepuBleLog.d("TmbBleManager initUUID")
    }

    override fun initialize() {
        LepuBleLog.d("TmbBleManager initialize")
    }

    override fun dealReqQueue(requestQueue: RequestQueue): RequestQueue {
        requestQueue.add(enableNotifications(notify_char_ack))
            .add(enableIndications(indicate_char))
        LepuBleLog.d("TmbBleManager dealReqQueue")
        return requestQueue
    }

    override fun getGattCallback(): BleManagerGattCallback {

        return object : BleManagerGattCallback(){
            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                LepuBleLog.d(MANAGER_TAG, "service_uuid = $service_uuid,write_uuid = $write_uuid,notify_uuid = $notify_uuid,isUpdater = $isUpdater")

                val service_info = gatt.getService(service_info_uuid)
                LepuBleLog.d(MANAGER_TAG, "service_info_uuid ==  $service_info")

                service_info?.let {
                    manufacturer_char = service_info.getCharacteristic(manufacturer_uuid)
                    model_char = service_info.getCharacteristic(model_uuid)
                    serial_char = service_info.getCharacteristic(serial_uuid)
                    hv_char = service_info.getCharacteristic(hv_uuid)
                    fv_char = service_info.getCharacteristic(fv_uuid)
                    sv_char = service_info.getCharacteristic(sv_uuid)
                    id_char = service_info.getCharacteristic(id_uuid)

                    LepuBleLog.d(MANAGER_TAG, "manufacturer_char ==  $manufacturer_char")
                    LepuBleLog.d(MANAGER_TAG, "model_char ==  $model_char")
                    LepuBleLog.d(MANAGER_TAG, "serial_char ==  $serial_char")
                    LepuBleLog.d(MANAGER_TAG, "hv_char ==  $hv_char")
                    LepuBleLog.d(MANAGER_TAG, "fv_char ==  $fv_char")
                    LepuBleLog.d(MANAGER_TAG, "sv_char ==  $sv_char")
                    LepuBleLog.d(MANAGER_TAG, "id_char ==  $id_char")
                }

                val service = gatt.getService(service_uuid)
                LepuBleLog.d(MANAGER_TAG, "service ==  $service")

                service?.let {
                    write_char = service.getCharacteristic(write_uuid)
                    write_char_ack = service.getCharacteristic(write_uuid_ack)
                    notify_char = service.getCharacteristic(notify_uuid)
                    notify_char_ack = service.getCharacteristic(notify_uuid_ack)
                    indicate_char = service.getCharacteristic(indicate_uuid)

                    LepuBleLog.d(MANAGER_TAG, "write_char ==  $write_char")
                    LepuBleLog.d(MANAGER_TAG, "write_char_ack ==  $write_char_ack")
                    LepuBleLog.d(MANAGER_TAG, "notify_char ==  $notify_char")
                    LepuBleLog.d(MANAGER_TAG, "notify_char_ack ==  $notify_char_ack")
                    LepuBleLog.d(MANAGER_TAG, "indicate_char ==  $indicate_char")
                }?: kotlin.run {
                    if (isUpdater) return true
                }

                var notify = false
                notify_char?.let{

                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "notify_char properties ==  $properties")

                    notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                    LepuBleLog.d(MANAGER_TAG, "notify_char notify ==  $notify")
                }
                var notify_ack = false
                notify_char_ack?.let{

                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "notify_char_ack properties ==  $properties")

                    notify_ack = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                    LepuBleLog.d(MANAGER_TAG, "notify_char_ack notify_ack ==  $notify_ack")
                }
                var indicate = false
                indicate_char?.let{

                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "indicate_char properties ==  $properties")

                    indicate = properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
                    LepuBleLog.d(MANAGER_TAG, "indicate_char indicate ==  $indicate")
                }

                var writeRequest = false
                write_char?.let {
                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "write_char properties ==  $properties")

                    var writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                        writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    }
                    it.writeType = writeType

                    writeRequest =
                        properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 || properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                    LepuBleLog.d(MANAGER_TAG, "write_char writeRequest ==  $writeRequest")
                }
                var writeAckRequest = false
                write_char_ack?.let {
                    val properties = it.properties
                    LepuBleLog.d(MANAGER_TAG, "write_char_ack properties ==  $properties")

                    var writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                        writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    }
                    it.writeType = writeType

                    writeAckRequest =
                        properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 || properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                    LepuBleLog.d(MANAGER_TAG, "write_char_ack writeAckRequest ==  $writeAckRequest")
                }

                // Return true if all required services have been found
                return write_char != null && notify_char != null && write_char_ack != null && notify_char_ack != null && indicate_char != null
                        && notify && writeRequest && notify_ack && writeAckRequest && indicate
            }

            override fun initialize() {
                super.initialize()

                LepuBleLog.d(MANAGER_TAG, "initialize")

                buildRequestQueue()
                setNotify()
                setNotifyAck()
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

    fun setNotifyAck() {
        LepuBleLog.d(MANAGER_TAG, "setNotifyAck...")
        setNotificationCallback(notify_char_ack)
            .with { device: BluetoothDevice, data: Data ->

                data.value?.let {
                    LepuBleLog.d(
                        MANAGER_TAG,
                        device.name + " NotificationCallback received==" + bytesToHex(it) + " size=" + bytesToHex(
                            it
                        ).length
                    )

                } ?: kotlin.run {
                    log(Log.WARN, "NotificationCallback data.value == null")
                }

                notifyListener?.let {
                    it.onNotify(device, data)
                } ?: kotlin.run {
                    log(Log.WARN, "NotificationCallback listener == null")
                }
            }
    }
    fun sendCmdAck(bytes: ByteArray) {
        LepuBleLog.e("sendCmdAck--------------" + bytesToHex(bytes))
        writeCharacteristic(write_char_ack, bytes)
            .split()
            .done { device: BluetoothDevice ->
                LepuBleLog.e(device.name + " done send cmd:" + bytesToHex(bytes))
            }.fail { device, status ->
                LepuBleLog.e(device.name + " fail send cmd:" + bytesToHex(bytes))
            }
            .enqueue()
    }
    fun readInfo() {
        readCharacteristic(manufacturer_char).with { device, data ->
            data.value?.let {
                tmbInfo.manufacturer = trimStr(String(it))
            }
        }.enqueue()
        readCharacteristic(model_char).with { device, data ->
            data.value?.let {
                tmbInfo.name = trimStr(String(it))
            }
        }.enqueue()
        readCharacteristic(serial_char).with { device, data ->
            data.value?.let {
                tmbInfo.serial = trimStr(String(it))
            }
        }.enqueue()
        readCharacteristic(hv_char).with { device, data ->
            data.value?.let {
                tmbInfo.hv = trimStr(String(it))
            }
        }.enqueue()
        readCharacteristic(fv_char).with { device, data ->
            data.value?.let {
                tmbInfo.fv = trimStr(String(it))
            }
        }.enqueue()
        readCharacteristic(sv_char).with { device, data ->
            data.value?.let {
                tmbInfo.sv = trimStr(String(it))
            }
        }.enqueue()
    }

}