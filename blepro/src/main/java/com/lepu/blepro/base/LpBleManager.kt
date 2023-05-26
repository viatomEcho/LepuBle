package com.lepu.blepro.base

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.RequestQueue
import no.nordicsemi.android.ble.data.Data
import java.util.*

/**
 * @ClassName LpBleManager
 * @Description TODO
 * @Author wujuan
 * @Date 2021/11/12 16:59
 */

const val MANAGER_TAG = "LpBleManager"

abstract class LpBleManager(context: Context): BleManager(context) {


    lateinit var service_uuid: UUID
    lateinit var write_uuid: UUID
    lateinit var notify_uuid: UUID
    lateinit var indicate_uuid: UUID

    var indicate_char: BluetoothGattCharacteristic? = null
    var write_char: BluetoothGattCharacteristic? = null

    var notify_char:BluetoothGattCharacteristic? = null

    var notifyListener: NotifyListener? = null

    // 升级固件
    var isUpdater = false



    init {
        initUUID()
    }

    abstract fun initUUID()
    protected abstract fun initialize()

    override fun getGattCallback(): BleManagerGattCallback {
        if (!this::service_uuid.isInitialized || !this::write_uuid.isInitialized || !this::notify_uuid.isInitialized){
            LepuBleLog.e(MANAGER_TAG, "check uuid !!!")
            LepuBleLog.e(MANAGER_TAG, "check uuid !!!")
        }


        return object : BleManagerGattCallback(){
            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                LepuBleLog.d(MANAGER_TAG, "service_uuid = $service_uuid,write_uuid = $write_uuid,notify_uuid = $notify_uuid,isUpdater = $isUpdater")


                val service = gatt.getService(service_uuid)
                LepuBleLog.d(MANAGER_TAG, "service ==  $service")
                
                service?.let {
                    if (this@LpBleManager::indicate_uuid.isInitialized) {
                        indicate_char = service.getCharacteristic(indicate_uuid)
                    }
                    write_char = service.getCharacteristic(write_uuid)
                    notify_char = service.getCharacteristic(notify_uuid)

                    if (indicate_char != null) {
                        LepuBleLog.d(MANAGER_TAG, "indicate_char ==  $indicate_char")
                    }
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
                return write_char != null && notify_char != null && notify && writeRequest
            }

            override fun initialize() {
                super.initialize()

                LepuBleLog.d(MANAGER_TAG, "initialize")

                buildRequestQueue()
                setNotify()
                this@LpBleManager.initialize()

            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int) {
                super.onMtuChanged(gatt, mtu)
                log(Log.INFO, "onMtuChanged mtu == $mtu")
            }

            override fun onConnectionUpdated(gatt: BluetoothGatt, interval: Int, latency: Int, timeout: Int) {
                super.onConnectionUpdated(gatt, interval, latency, timeout)
                log(Log.INFO, "onConnectionUpdated interval == $interval, latency == $latency, timeout == $timeout")
            }

            override fun onDeviceDisconnected() {
                indicate_char = null
                write_char = null
                notify_char = null
            }

        }
    }
    fun setNotify() {
        setNotificationCallback(notify_char)
            .with { device: BluetoothDevice, data: Data ->

                data.value?.let {
                    LepuBleLog.d(MANAGER_TAG, device.name + " NotificationCallback received==" + bytesToHex(it) + " size=" + bytesToHex(it).length)

                }?: kotlin.run {
                    log(Log.WARN, "NotificationCallback data.value == null")
                }

                notifyListener?.let {
                   it.onNotify(device, data)
                }?: kotlin.run {
                    log(Log.WARN, "NotificationCallback listener == null")
                }
            }

        if (indicate_char != null) {
            setIndicationCallback(indicate_char).with { device, data ->
                data.value?.let {
                    LepuBleLog.d(
                        MANAGER_TAG,
                        device.name + " IndicationCallback received==" + bytesToHex(it) + " size=" + bytesToHex(
                            it
                        ).length
                    )

                } ?: kotlin.run {
                    log(Log.WARN, "IndicationCallback data.value == null")
                }

                notifyListener?.let {
                    it.onNotify(device, data)
                } ?: kotlin.run {
                    log(Log.WARN, "IndicationCallback listener == null")
                }
            }
        }
    }

    open fun buildRequestQueue() {
        LepuBleLog.d(MANAGER_TAG, "buildRequestQueue...")

        val queue = beginAtomicRequestQueue()
//            .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
//                .with { device: BluetoothDevice?, mtu: Int ->
//                    log(Log.INFO, "MTU set to $mtu")
//                }
//                .fail { device: BluetoothDevice?, status: Int ->
//                    log(Log.WARN, "Requested MTU not supported: $status")
//                })
            //                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
            //                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
            //                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
            .add(enableNotifications(notify_char))
            .done { device: BluetoothDevice? ->
                log(Log.INFO, "Target initialized")
            }
        dealReqQueue(queue).enqueue()
    }

    fun setBleMtu(mtu1: Int) {
        log(Log.INFO, "setBleMtu")
        beginAtomicRequestQueue().add(requestMtu(mtu1) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
            .with { device: BluetoothDevice?, mtu: Int ->
                log(Log.INFO, "MTU set to $mtu")
            }
            .fail { device: BluetoothDevice?, status: Int ->
                log(Log.WARN, "Requested MTU not supported: $status")
            })
            .enqueue()
    }

    fun getBleMtu(): Int {
        return mtu
    }

    /**
     * 子类区别处理RequestQueue， 不需特殊处理时候返回默认
     */
    abstract fun dealReqQueue(requestQueue: RequestQueue): RequestQueue

    open fun sendCmd(bytes: ByteArray) {
        LepuBleLog.e("sendCmd--------------" + bytesToHex(bytes))
        writeCharacteristic(write_char, bytes)
            .split()
            .done { device: BluetoothDevice ->
                LepuBleLog.e(device.name + " done send cmd:" + bytesToHex(bytes))
            }.fail { device, status ->
                LepuBleLog.e(device.name + " fail send cmd:" + bytesToHex(bytes))
            }
            .enqueue()
    }

    override fun log(priority: Int, message: String) {
        LepuBleLog.d(MANAGER_TAG, message)
    }
    
}