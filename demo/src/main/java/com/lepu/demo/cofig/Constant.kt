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
            const val ER1_N_MODEL: Int = Bluetooth.MODEL_ER1_N
            const val DUOEK_MODEL: Int = Bluetooth.MODEL_DUOEK
            const val ER2_MODEL: Int = Bluetooth.MODEL_ER2
            const val PC80B_MODEL: Int = Bluetooth.MODEL_PC80B
            const val BP2_MODEL: Int = Bluetooth.MODEL_BP2
            const val BP2A_MODEL: Int = Bluetooth.MODEL_BP2A
            const val BP2T_MODEL: Int = Bluetooth.MODEL_BP2T
            const val BP2W_MODEL: Int = Bluetooth.MODEL_BP2W
            const val LE_BP2W_MODEL: Int = Bluetooth.MODEL_LE_BP2W
            const val BPM_MODEL: Int = Bluetooth.MODEL_BPM
            const val O2RING_MODEL: Int = Bluetooth.MODEL_O2RING
            const val BABYO2_MODEL: Int = Bluetooth.MODEL_BABYO2
            const val KIDSO2_MODEL: Int = Bluetooth.MODEL_KIDSO2
            const val OXYLINK_MODEL: Int = Bluetooth.MODEL_OXYLINK
            const val OXYFIT_MODEL: Int = Bluetooth.MODEL_OXYFIT
            const val SLEEPU_MODEL: Int = Bluetooth.MODEL_SLEEPU
            const val WEARO2_MODEL: Int = Bluetooth.MODEL_WEARO2
            const val SNOREO2_MODEL: Int = Bluetooth.MODEL_SNOREO2
            const val SLEEPO2_MODEL: Int = Bluetooth.MODEL_SLEEPO2
            const val O2M_MODEL: Int = Bluetooth.MODEL_O2M
            const val CHECKO2_MODEL: Int = Bluetooth.MODEL_CHECKO2
            const val BABYO2N_MODEL: Int = Bluetooth.MODEL_BABYO2N
            const val PC60FW_MODEL: Int = Bluetooth.MODEL_PC60FW
            const val PF_10_MODEL: Int = Bluetooth.MODEL_PF_10
            const val PF_20_MODEL: Int = Bluetooth.MODEL_PF_20
            const val POD_1W_MODEL: Int = Bluetooth.MODEL_POD_1W
            const val PC_60NW_1_MODEL: Int = Bluetooth.MODEL_PC_60NW_1
            const val PC_60NW_MODEL: Int = Bluetooth.MODEL_PC_60NW
            const val POD2B_MODEL: Int = Bluetooth.MODEL_POD2B
            const val PC_60B_MODEL: Int = Bluetooth.MODEL_PC_60B
            const val OXYSMART_MODEL: Int = Bluetooth.MODEL_OXYSMART
            const val TV221U_MODEL: Int = Bluetooth.MODEL_TV221U
            const val AOJ20A_MODEL: Int = Bluetooth.MODEL_AOJ20A
            const val FHR_MODEL: Int = Bluetooth.MODEL_FHR
            const val PC100_MODEL: Int = Bluetooth.MODEL_PC100
            const val PC6N_MODEL: Int = Bluetooth.MODEL_PC66B
            const val AP20_MODEL: Int = Bluetooth.MODEL_AP20
            const val SP20_MODEL: Int = Bluetooth.MODEL_SP20
            const val LEW3_MODEL: Int = Bluetooth.MODEL_LEW3
            const val VETCORDER_MODEL: Int = Bluetooth.MODEL_VETCORDER
            const val CHECK_ADV_MODEL: Int = Bluetooth.MODEL_CHECK_ADV
            const val CHECK_POD_MODEL: Int = Bluetooth.MODEL_CHECK_POD
            const val CHECKME_LE_MODEL: Int = Bluetooth.MODEL_CHECKME_LE
            const val PC_68B_MODEL: Int = Bluetooth.MODEL_PC_68B
            const val PC300_MODEL: Int = Bluetooth.MODEL_PC300
            const val PULSEBITEX_MODEL: Int = Bluetooth.MODEL_PULSEBITEX
            const val FETAL_MODEL: Int = Bluetooth.MODEL_FETAL
            const val VTM_AD5_MODEL: Int = Bluetooth.MODEL_VTM_AD5
            const val VCOMIN_MODEL: Int = Bluetooth.MODEL_VCOMIN
            const val LEM_MODEL: Int = Bluetooth.MODEL_LEM
            const val LES1_MODEL: Int = Bluetooth.MODEL_LES1
            const val F4_SCALE_MODEL: Int = Bluetooth.MODEL_F4_SCALE
            const val F5_SCALE_MODEL: Int = Bluetooth.MODEL_F5_SCALE
            const val F8_SCALE_MODEL: Int = Bluetooth.MODEL_F8_SCALE
            const val PATIENT_DEVICE_JSON: Int = 1004

            val SUPPORT_MODELS = intArrayOf(ER1_MODEL, DUOEK_MODEL, ER2_MODEL, PC80B_MODEL, BP2_MODEL,
                BP2A_MODEL, BPM_MODEL, O2RING_MODEL, PC60FW_MODEL, FHR_MODEL, PC100_MODEL, PC6N_MODEL,
                AP20_MODEL, LEW3_MODEL, VETCORDER_MODEL, BP2W_MODEL, BABYO2_MODEL, ER1_N_MODEL, SP20_MODEL,
                LE_BP2W_MODEL, TV221U_MODEL, AOJ20A_MODEL, BABYO2N_MODEL, CHECKO2_MODEL, O2M_MODEL,
                SLEEPO2_MODEL, SNOREO2_MODEL, WEARO2_MODEL, SLEEPU_MODEL, OXYLINK_MODEL, KIDSO2_MODEL,
                OXYSMART_MODEL, OXYFIT_MODEL, POD_1W_MODEL, CHECK_POD_MODEL, PC_68B_MODEL, BP2T_MODEL,
                POD2B_MODEL, PC_60NW_1_MODEL, PC_60NW_MODEL, PC_60B_MODEL, PC300_MODEL, PULSEBITEX_MODEL, FETAL_MODEL,
                VTM_AD5_MODEL, VCOMIN_MODEL, CHECKME_LE_MODEL, PF_10_MODEL, PF_20_MODEL, LEM_MODEL, LES1_MODEL,
                CHECK_ADV_MODEL, F4_SCALE_MODEL, F5_SCALE_MODEL, F8_SCALE_MODEL
            )

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