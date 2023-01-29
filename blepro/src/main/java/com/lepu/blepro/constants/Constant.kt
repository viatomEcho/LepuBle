package com.lepu.blepro.constants

object Constant {
    /**
     * ap20配置参数类型
     */
    interface Ap20ConfigType {
        companion object {
            /**
             * 背光等级
             */
            const val BACK_LIGHT = 0

            /**
             * 警报功能开关
             */
            const val ALARM_SWITCH = 1

            /**
             * 血氧过低阈值
             */
            const val LOW_OXY_THRESHOLD = 2

            /**
             * 脉率过低阈值
             */
            const val LOW_HR_THRESHOLD = 3

            /**
             * 脉率过高阈值
             */
            const val HIGH_HR_THRESHOLD = 4
        }
    }

    /**
     * ap20使能类型
     */
    interface Ap20EnableType {
        companion object {
            /**
             * 使能血氧参数
             */
            const val OXY_PARAM = 0

            /**
             * 使能血氧波形
             */
            const val OXY_WAVE = 1

            /**
             * 使能鼻息流参数
             */
            const val BREATH_PARAM = 2

            /**
             * 使能鼻息流波形
             */
            const val BREATH_WAVE = 3

        }
    }

    /**
     * pc60fw使能类型
     */
    interface Pc60fwEnableType {
        companion object {
            /**
             * 使能血氧参数
             */
            const val OXY_PARAM = 0

            /**
             * 使能血氧波形
             */
            const val OXY_WAVE = 1
        }
    }

    /**
     * pod1w使能类型
     */
    interface Pod1wEnableType {
        companion object {
            /**
             * 使能血氧参数
             */
            const val OXY_PARAM = 0

            /**
             * 使能血氧波形
             */
            const val OXY_WAVE = 1
        }
    }

    /**
     * pc68b使能类型
     */
    interface Pc68bEnableType {
        companion object {
            /**
             * 使能血氧参数
             */
            const val OXY_PARAM = 0

            /**
             * 使能血氧波形
             */
            const val OXY_WAVE = 1
        }
    }

    /**
     * checkmele列表类型
     */
    interface CheckmeLeListType {
        companion object {
            const val ECG_TYPE = 0
            const val OXY_TYPE = 1
            const val DLC_TYPE = 2
            const val TEMP_TYPE = 3
        }
    }

    /**
     * sp20配置参数类型
     */
    interface Sp20ConfigType {
        companion object {
            /**
             * 警报功能开关
             */
            const val ALARM_SWITCH = 1

            /**
             * 血氧过低阈值
             */
            const val LOW_OXY_THRESHOLD = 2

            /**
             * 脉率过低阈值
             */
            const val LOW_HR_THRESHOLD = 3

            /**
             * 脉率过高阈值
             */
            const val HIGH_HR_THRESHOLD = 4

            /**
             * 搏动音开关
             */
            const val PULSE_BEEP = 5
        }
    }

    /**
     * sp20使能类型
     */
    interface Sp20EnableType {
        companion object {
            /**
             * 使能血氧参数
             */
            const val OXY_PARAM = 0

            /**
             * 使能血氧波形
             */
            const val OXY_WAVE = 1
        }
    }

    /**
     * lem按摩时间
     */
    interface LemMassageTime {
        companion object {
            const val MIN_15 = 0
            const val MIN_10 = 1
            const val MIN_5 = 2
        }
    }
    /**
     * lem按摩模式
     */
    interface LemMassageMode {
        companion object {
            const val VITALITY = 0
            const val DYNAMIC = 1
            const val HAMMERING = 2
            const val SOOTHING = 3
            const val AUTOMATIC = 4
        }
    }

    /**
     * bp2切换状态
     */
    interface Bp2SwitchState {
        companion object {
            const val ENTER_BP = 0
            const val ENTER_ECG = 1
            const val ENTER_HISTORY = 2
            const val ENTER_ON = 3
            const val ENTER_OFF = 4
        }
    }

    /**
     * lp-bp2列表类型
     */
    interface LpBp2wListType {
        companion object {
            const val USER_TYPE = 0
            const val BP_TYPE = 1
            const val ECG_TYPE = 2
        }
    }
}