package com.lepu.demo.cofig

import android.util.SparseArray
import com.lepu.blepro.objs.Bluetooth

/**
 * author: wujuan
 * created on: 2021/7/14 14:46
 * description:
 */
class Constant{

    interface BluetoothConfig{
        companion object{

            const val ER1_MODEL: Int = Bluetooth.MODEL_ER1
            const val DUOEK_MODEL: Int = Bluetooth.MODEL_DUOEK
            const val ER2_MODEL: Int = Bluetooth.MODEL_ER2
            const val PC80B_MODEL: Int = Bluetooth.MODEL_PC80B
            const val BP2_MODEL: Int = Bluetooth.MODEL_BP2
            const val BP2A_MODEL: Int = Bluetooth.MODEL_BP2A
            const val BPM_MODEL: Int = Bluetooth.MODEL_BPM
            const val O2RING_MODEL: Int = Bluetooth.MODEL_O2RING
            const val PC60FW_MODEL: Int = Bluetooth.MODEL_PC60FW
            const val FHR_MODEL: Int = Bluetooth.MODEL_FHR
            const val PC100_MODEL: Int = Bluetooth.MODEL_PC100
            const val PC6N_MODEL: Int = Bluetooth.MODEL_PC_6N
            const val PATIENT_DEVICE_JSON: Int = 1004

            val SUPPORT_MODELS = intArrayOf(ER1_MODEL, DUOEK_MODEL, ER2_MODEL, PC80B_MODEL, BP2_MODEL,
                BP2A_MODEL, BPM_MODEL, O2RING_MODEL, PC60FW_MODEL, FHR_MODEL, PC100_MODEL, PC6N_MODEL)

            val SUPPORT_FACES = SparseArray<Int>().apply {
               for (m in SUPPORT_MODELS){
                   this.put(m, m)
               }
            }

            /**
             * 当前需要扫描的设备数组
             */
            var currentScanModel: IntArray = SUPPORT_MODELS

            const val CHECK_BLE_REQUEST_CODE = 6001

            var bleSdkServiceEnable: Boolean = false
            var singleConnect: Boolean = true

            var currentModel: IntArray = if (singleConnect) IntArray(1) else SUPPORT_MODELS





        }
    }

    interface Event{
        companion object{
        }
    }

}