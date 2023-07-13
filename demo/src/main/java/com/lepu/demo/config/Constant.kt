package com.lepu.demo.config

import android.util.SparseArray
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.data.BpData
import com.lepu.demo.data.EcgData
import com.lepu.demo.data.EcnData
import com.lepu.demo.data.OxyData

/**
 * author: wujuan
 * created on: 2021/7/14 14:46
 * description:
 */
class Constant{

    interface BluetoothConfig{
        companion object{

            const val ER1_MODEL: Int = Bluetooth.MODEL_ER1
            const val HHM1_MODEL: Int = Bluetooth.MODEL_HHM1
            const val ER1_N_MODEL: Int = Bluetooth.MODEL_ER1_N
            const val DUOEK_MODEL: Int = Bluetooth.MODEL_DUOEK
            const val HHM2_MODEL: Int = Bluetooth.MODEL_HHM2
            const val HHM3_MODEL: Int = Bluetooth.MODEL_HHM3
            const val ER2_MODEL: Int = Bluetooth.MODEL_ER2
            const val LP_ER2_MODEL: Int = Bluetooth.MODEL_LP_ER2
            const val PC80B_MODEL: Int = Bluetooth.MODEL_PC80B
            const val PC80B_BLE_MODEL: Int = Bluetooth.MODEL_PC80B_BLE
            const val PC80B_BLE2_MODEL: Int = Bluetooth.MODEL_PC80B_BLE2
            const val BP2_MODEL: Int = Bluetooth.MODEL_BP2
            const val BP2A_MODEL: Int = Bluetooth.MODEL_BP2A
            const val BP2T_MODEL: Int = Bluetooth.MODEL_BP2T
            const val BP2W_MODEL: Int = Bluetooth.MODEL_BP2W
            const val LE_BP2W_MODEL: Int = Bluetooth.MODEL_LE_BP2W
            const val BPM_MODEL: Int = Bluetooth.MODEL_BPM
            const val O2RING_MODEL: Int = Bluetooth.MODEL_O2RING
            const val AI_S100_MODEL: Int = Bluetooth.MODEL_AI_S100
            const val CMRING_MODEL: Int = Bluetooth.MODEL_CMRING
            const val OXYRING_MODEL: Int = Bluetooth.MODEL_OXYRING
            const val BABYO2_MODEL: Int = Bluetooth.MODEL_BABYO2
            const val BBSM_S1_MODEL: Int = Bluetooth.MODEL_BBSM_S1
            const val BBSM_S2_MODEL: Int = Bluetooth.MODEL_BBSM_S2
            const val KIDSO2_MODEL: Int = Bluetooth.MODEL_KIDSO2
            const val KIDSO2_WPS_MODEL: Int = Bluetooth.MODEL_KIDSO2_WPS
            const val OXYLINK_MODEL: Int = Bluetooth.MODEL_OXYLINK
            const val SI_PO6_MODEL: Int = Bluetooth.MODEL_SI_PO6
            const val OXYFIT_MODEL: Int = Bluetooth.MODEL_OXYFIT
            const val OXYFIT_WPS_MODEL: Int = Bluetooth.MODEL_OXYFIT_WPS
            const val SLEEPU_MODEL: Int = Bluetooth.MODEL_SLEEPU
            const val WEARO2_MODEL: Int = Bluetooth.MODEL_WEARO2
            const val SNOREO2_MODEL: Int = Bluetooth.MODEL_SNOREO2
            const val SLEEPO2_MODEL: Int = Bluetooth.MODEL_SLEEPO2
            const val O2M_MODEL: Int = Bluetooth.MODEL_O2M
            const val O2M_WPS_MODEL: Int = Bluetooth.MODEL_O2M_WPS
            const val CHECKO2_MODEL: Int = Bluetooth.MODEL_CHECKO2
            const val BABYO2N_MODEL: Int = Bluetooth.MODEL_BABYO2N
            const val PC60FW_MODEL: Int = Bluetooth.MODEL_PC60FW
            const val PF_10_MODEL: Int = Bluetooth.MODEL_PF_10
            const val PF_10AW_MODEL: Int = Bluetooth.MODEL_PF_10AW
            const val PF_10AW1_MODEL: Int = Bluetooth.MODEL_PF_10AW1
            const val PF_10BW_MODEL: Int = Bluetooth.MODEL_PF_10BW
            const val PF_10BW1_MODEL: Int = Bluetooth.MODEL_PF_10BW1
            const val PF_20_MODEL: Int = Bluetooth.MODEL_PF_20
            const val PF_20AW_MODEL: Int = Bluetooth.MODEL_PF_20AW
            const val PF_20B_MODEL: Int = Bluetooth.MODEL_PF_20B
            const val POD_1W_MODEL: Int = Bluetooth.MODEL_POD_1W
            const val PC_60NW_1_MODEL: Int = Bluetooth.MODEL_PC_60NW_1
            const val PC_60NW_MODEL: Int = Bluetooth.MODEL_PC_60NW
            const val PC60NW_NO_SN_MODEL: Int = Bluetooth.MODEL_PC_60NW_NO_SN
            const val PC60NW_BLE_MODEL: Int = Bluetooth.MODEL_PC60NW_BLE
            const val PC60NW_WPS_MODEL: Int = Bluetooth.MODEL_PC60NW_WPS
            const val POD2B_MODEL: Int = Bluetooth.MODEL_POD2B
            const val PC_60B_MODEL: Int = Bluetooth.MODEL_PC_60B
            const val OXYSMART_MODEL: Int = Bluetooth.MODEL_OXYSMART
            const val TV221U_MODEL: Int = Bluetooth.MODEL_TV221U
            const val AOJ20A_MODEL: Int = Bluetooth.MODEL_AOJ20A
            const val FHR_MODEL: Int = Bluetooth.MODEL_FHR
            const val PC100_MODEL: Int = Bluetooth.MODEL_PC100
            const val PC6N_MODEL: Int = Bluetooth.MODEL_PC66B
            const val AP20_MODEL: Int = Bluetooth.MODEL_AP20
            const val AP20_WPS_MODEL: Int = Bluetooth.MODEL_AP20_WPS
            const val SP20_MODEL: Int = Bluetooth.MODEL_SP20
            const val SP20_WPS_MODEL: Int = Bluetooth.MODEL_SP20_WPS
            const val SP20_BLE_MODEL: Int = Bluetooth.MODEL_SP20_BLE
            const val LEW_MODEL: Int = Bluetooth.MODEL_LEW
            const val W12C_MODEL: Int = Bluetooth.MODEL_W12C
            const val VETCORDER_MODEL: Int = Bluetooth.MODEL_VETCORDER
            const val CHECK_ADV_MODEL: Int = Bluetooth.MODEL_CHECK_ADV
            const val CHECK_POD_MODEL: Int = Bluetooth.MODEL_CHECK_POD
            const val CHECKME_POD_WPS_MODEL: Int = Bluetooth.MODEL_CHECKME_POD_WPS
            const val CHECKME_LE_MODEL: Int = Bluetooth.MODEL_CHECKME_LE
            const val CHECKME_MODEL: Int = Bluetooth.MODEL_CHECKME
            const val PC_68B_MODEL: Int = Bluetooth.MODEL_PC_68B
            const val PC300_MODEL: Int = Bluetooth.MODEL_PC300
            const val PC300_BLE_MODEL: Int = Bluetooth.MODEL_PC300_BLE
            const val GM_300SNT_MODEL: Int = Bluetooth.MODEL_GM_300SNT
            const val PULSEBITEX_MODEL: Int = Bluetooth.MODEL_PULSEBITEX
            const val HHM4_MODEL: Int = Bluetooth.MODEL_HHM4
            const val FETAL_MODEL: Int = Bluetooth.MODEL_FETAL
            const val VTM_AD5_MODEL: Int = Bluetooth.MODEL_VTM_AD5
            const val VCOMIN_MODEL: Int = Bluetooth.MODEL_VCOMIN
            const val LEM_MODEL: Int = Bluetooth.MODEL_LEM
            const val LES1_MODEL: Int = Bluetooth.MODEL_LES1
            const val F4_SCALE_MODEL: Int = Bluetooth.MODEL_F4_SCALE
            const val MY_SCALE_MODEL: Int = Bluetooth.MODEL_MY_SCALE
            const val F5_SCALE_MODEL: Int = Bluetooth.MODEL_F5_SCALE
            const val F8_SCALE_MODEL: Int = Bluetooth.MODEL_F8_SCALE
            const val LPRE_MODEL: Int = Bluetooth.MODEL_LPRE
            const val LE_B1_MODEL: Int = Bluetooth.MODEL_LE_B1
            const val OXYU_MODEL: Int = Bluetooth.MODEL_OXYU
            const val S5W_MODEL: Int = Bluetooth.MODEL_S5W
            const val S6W_MODEL: Int = Bluetooth.MODEL_S6W
            const val S6W1_MODEL: Int = Bluetooth.MODEL_S6W1
            const val S7W_MODEL: Int = Bluetooth.MODEL_S7W
            const val S7BW_MODEL: Int = Bluetooth.MODEL_S7BW
            const val LPM311_MODEL: Int = Bluetooth.MODEL_LPM311
            const val POCTOR_M3102_MODEL: Int = Bluetooth.MODEL_POCTOR_M3102
            const val BIOLAND_BGM_MODEL: Int = Bluetooth.MODEL_BIOLAND_BGM
            const val ER3_MODEL: Int = Bluetooth.MODEL_ER3
            const val LEPOD_MODEL: Int = Bluetooth.MODEL_LEPOD
            const val VTM01_MODEL: Int = Bluetooth.MODEL_VTM01
            const val PC200_BLE_MODEL: Int = Bluetooth.MODEL_PC200_BLE
            const val BTP_MODEL: Int = Bluetooth.MODEL_BTP
            const val S5_SCALE_MODEL: Int = Bluetooth.MODEL_S5_SCALE
            const val ventilator_MODEL: Int = Bluetooth.MODEL_R20
            const val R21_MODEL: Int = Bluetooth.MODEL_R21
            const val R10_MODEL: Int = Bluetooth.MODEL_R10
            const val R11_MODEL: Int = Bluetooth.MODEL_R11
            const val LERES_MODEL: Int = Bluetooth.MODEL_LERES
            const val ECN_MODEL: Int = Bluetooth.MODEL_ECN
            const val LP_BP3W_MODEL: Int = Bluetooth.MODEL_LP_BP3W
            const val LP_BP3C_MODEL: Int = Bluetooth.MODEL_LP_BP3C
            const val LESCALE_P3_MODEL: Int = Bluetooth.MODEL_LESCALE_P3
            const val PATIENT_DEVICE_JSON: Int = 1004

            val SUPPORT_MODELS = intArrayOf(ER1_MODEL, DUOEK_MODEL, ER2_MODEL, PC80B_MODEL, BP2_MODEL,
                BP2A_MODEL, BPM_MODEL, O2RING_MODEL, PC60FW_MODEL, FHR_MODEL, PC100_MODEL, PC6N_MODEL,
                AP20_MODEL, LEW_MODEL, VETCORDER_MODEL, BP2W_MODEL, BABYO2_MODEL, ER1_N_MODEL, SP20_MODEL,
                LE_BP2W_MODEL, TV221U_MODEL, AOJ20A_MODEL, BABYO2N_MODEL, CHECKO2_MODEL, O2M_MODEL,
                SLEEPO2_MODEL, SNOREO2_MODEL, WEARO2_MODEL, SLEEPU_MODEL, OXYLINK_MODEL, KIDSO2_MODEL,
                OXYSMART_MODEL, OXYFIT_MODEL, POD_1W_MODEL, CHECK_POD_MODEL, PC_68B_MODEL, BP2T_MODEL,
                POD2B_MODEL, PC_60NW_1_MODEL, PC_60NW_MODEL, PC_60B_MODEL, PC300_MODEL, PULSEBITEX_MODEL, FETAL_MODEL,
                VTM_AD5_MODEL, VCOMIN_MODEL, CHECKME_LE_MODEL, PF_10_MODEL, PF_20_MODEL, LEM_MODEL, LES1_MODEL,
                CHECK_ADV_MODEL, F4_SCALE_MODEL, MY_SCALE_MODEL, F5_SCALE_MODEL, F8_SCALE_MODEL, OXYRING_MODEL,
                BBSM_S1_MODEL, BBSM_S2_MODEL, CMRING_MODEL, LPRE_MODEL, LE_B1_MODEL, OXYU_MODEL, S5W_MODEL, W12C_MODEL,
                AI_S100_MODEL, HHM1_MODEL, HHM2_MODEL, HHM3_MODEL, HHM4_MODEL, LP_ER2_MODEL, S6W_MODEL, S7W_MODEL,
                LPM311_MODEL, POCTOR_M3102_MODEL, BIOLAND_BGM_MODEL, S7BW_MODEL, S6W1_MODEL,
                PF_10AW_MODEL, PF_10AW1_MODEL, PF_10BW_MODEL, PF_10BW1_MODEL, PF_20AW_MODEL, PF_20B_MODEL, CHECKME_MODEL,
                PC80B_BLE_MODEL, SP20_BLE_MODEL, PC300_BLE_MODEL, ER3_MODEL, LEPOD_MODEL, PC60NW_BLE_MODEL,
                SP20_WPS_MODEL, AP20_WPS_MODEL, PC60NW_WPS_MODEL, O2M_WPS_MODEL, PC80B_BLE2_MODEL, VTM01_MODEL,
                PC200_BLE_MODEL, BTP_MODEL, S5_SCALE_MODEL, ventilator_MODEL, LERES_MODEL, PC60NW_NO_SN_MODEL, ECN_MODEL,
                GM_300SNT_MODEL, OXYFIT_WPS_MODEL, KIDSO2_WPS_MODEL, CHECKME_POD_WPS_MODEL, R21_MODEL, R10_MODEL, R11_MODEL,
                LP_BP3W_MODEL, LP_BP3C_MODEL, SI_PO6_MODEL, LESCALE_P3_MODEL
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


            var ecgData = EcgData()
            var oxyData = OxyData()
            var bpData = BpData()
            var ecnData = EcnData()

            var splitText = ""
            var splitType = 0
            var needPair = false
            var isEncrypt = false
        }
    }

    interface Event{
        companion object{
        }
    }

}