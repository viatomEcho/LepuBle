package com.lepu.blepro.base

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.lepu.blepro.utils.LepuBleLog
import com.lepu.blepro.utils.bytesToHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.*

abstract class BaseBleManagerNew(context: Context) : BleManager(context) {
    var service_uuid: UUID? = null
    var write_uuid: UUID? = null
    var notify_uuid: UUID? = null
    var write_char: BluetoothGattCharacteristic? = null
    var notify_char: BluetoothGattCharacteristic? = null
    private var listener: NotifyListener? = null
    var isUpdater = false

    fun setNotifyListener(listener: NotifyListener?) {
        this.listener = listener
    }

    abstract fun initUUID()
    override fun getGattCallback(): BleManagerGattCallback {
        return MyManagerGattCallback()
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private inner class MyManagerGattCallback : BleManagerGattCallback() {
        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            LepuBleLog.d(
                TAG,
                "id:$service_uuid,,,,,$write_uuid,,,,$notify_uuid,,,,isUpdater=$isUpdater"
            )


//            final BluetoothGattService service = gatt.getService(service_uuid);
//            if (service != null) {
//                write_char = service.getCharacteristic(write_uuid);
//                notify_char = service.getCharacteristic(notify_uuid);
//            }
//            // Validate properties
//            boolean notify = false;
//            if (notify_char != null) {
//                final int properties = notify_char.getProperties();
//                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
//            }
//            boolean writeRequest = false;
//            if (write_char != null) {
//                final int properties = write_char.getProperties();
//                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
//                write_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//            }
//            // Return true if all required services have been found
//            return write_char != null && notify_char != null
//                    && notify && writeRequest;
            val service = gatt.getService(service_uuid)
            LepuBleLog.d(TAG, "service ==  $service")
            if (isUpdater && service == null) return true
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid)
                notify_char = service.getCharacteristic(notify_uuid)
            }
            LepuBleLog.d(TAG, "writeChar ==  $write_char")
            LepuBleLog.d(TAG, "notifyChar ==  $notify_char")
            // Validate properties
            var notify = false
            if (notify_char != null) {
                val properties = notify_char!!.properties
                LepuBleLog.d(
                    TAG,
                    "notifyChar properties ==  $properties"
                )
                notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
                LepuBleLog.d(TAG, "notifyChar notify ==  $notify")
            }
            var writeRequest = false
            if (write_char != null) {
                val properties = write_char!!.properties
                LepuBleLog.d(TAG, "writeChar properties ==  $properties")
                var writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
                    writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                write_char!!.writeType = writeType
                writeRequest =
                    properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 || properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                LepuBleLog.d(
                    TAG,
                    "writeChar writeRequest ==  $writeRequest"
                )
            }
            // Return true if all required services have been found
            return write_char != null && notify_char != null && notify && writeRequest
        }

        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        override fun isOptionalServiceSupported(gatt: BluetoothGatt): Boolean {
            return super.isOptionalServiceSupported(gatt)
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        override fun initialize() {
//            beginAtomicRequestQueue()
//                    .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
//                            .with((device, mtu) -> LepuBleLog.d(TAG, "MTU set to " + mtu))
//                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
////                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
////                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
////                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
////                    .add(sleep(500))
//                    .add(enableNotifications(notify_char))
//                    .done(device -> LepuBleLog.d(TAG, "Target initialized"))
//                    .enqueue();
            LepuBleLog.d(TAG, "initialize")
            initReqQueue()
            setNotify()
            this@BaseBleManagerNew.init()
        }

        override fun onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            write_char = null
            notify_char = null
        }
    }

    protected abstract fun init()
    fun setNotify() {

            setNotificationCallback(notify_char)
                .with { device: BluetoothDevice, data: Data ->
                    GlobalScope.launch(Dispatchers.Main) {
                        LepuBleLog.d(
                            TAG,
                            device.name + " received==" + bytesToHex(data.value!!) + "size =" + bytesToHex(
                                data.value!!
                            ).length
                        )
                        LepuBleLog.d("notify start", gattCallback.toString())
                        LepuBleLog.d("thread", Thread.currentThread().name)
                        listener!!.onNotify(device, data)
                        LepuBleLog.d("notify", "end")
                    }

                }

    }

    abstract fun initReqQueue()
    fun sendCmd(bytes: ByteArray?) {
        LepuBleLog.d(
            TAG,
            "BaseBleManager send: " + bytesToHex(bytes!!) + "----write char----" + write_char
        )
        writeCharacteristic(write_char, bytes)
            .split()
            .done { device: BluetoothDevice? -> }
            .enqueue()
    }

    override fun log(priority: Int, message: String) {
        LepuBleLog.d(TAG, message)
    }

    companion object {
        const val TAG = "BaseBleManager"
    }

    init {
        initUUID()
    }
}