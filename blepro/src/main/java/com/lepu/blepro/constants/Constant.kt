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

    /**
     * lp-bp2服务器类型
     */
    interface LpBp2wServer {
        companion object {
            const val HOST_BP2_PRO_TEST = "212.129.241.54"
            const val HOST_BP2_PRO_ONLINE = "203.195.204.99"
            const val HOST_BP2_PRO_PORT = 7200
        }
    }

    /**
     * 呼吸机
     */
    interface VentilatorAlarmLevel {
        companion object {
            const val NONE = 0 // 正常
            const val LOW = 0 // 低,提示
            const val MIDDLE = 0 // 中
            const val HIGH = 0 // 高
            const val VERY_HIGH = 0 // 最高,系统故障
        }
    }

    interface VentilatorEventId {
        companion object {
            const val LINE_DISCONNECT = 1 // 管路断开
            const val LINE_CLOG = 2 // 管路阻塞
            const val HIGH_AIR_LEAKAGE = 3 // 漏气量高
            const val HIGH_RR = 4 // 呼吸频率高
            const val LOW_RR = 5 // 呼吸频率低
            const val LOW_TIDAL_VOLUME = 6 // 潮气量低
            const val MINUTES_VTL_LOW = 7 // 分钟通气量低
            const val HYPOXIC_SATURATION = 8 // 血氧饱和度低
            const val HR_PR_HIGH = 9 // 心率/脉率高
            const val HR_PR_LOW = 10 // 心率/脉率低
            const val APNEA = 11 // 窒息
            const val POWER_DOWN = 12 // 掉电

            //涡轮
            const val TURBINE_NOT_CONNECT = 101 // 涡轮HALL线没接
            const val TURBINE_TEMP_ERROR = 102 // 涡轮温度超过90度
            const val TURBINE_LOCKED_ROTOR = 103 // 涡轮堵转
            const val TURBINE_POWER_ERROR = 104 // 涡轮电源异常

            //加热盘
            const val HEATING_ERROR = 201 // 加热盘异常
            const val HEATING_NOT_CONNECT = 202 // 加热盘没接/温度传感器损坏
            const val HEATING_TEMP_ERROR = 203 // 加热盘温度超过75度

            //流量
            const val FLOW_SENSOR_ERROR = 301 // 流量传感器异常
            const val PRESSURE_SENSOR_ERROR = 302 // 压力传感器异常
            const val FLOW_COMM_ERROR = 303 // 流量传感器通信异常
            const val VTL_FLOW_TOO_LARGE = 304 // 流量传感器测得的流速过大
            const val VTL_FLOW_TOO_SMALL = 305 // 流量传感器测得的流速过小
            const val VTL_PRESSURE_TOO_LARGE = 306 // 正常通气时，压力传感器数值过大
            const val VTL_PRESSURE_TOO_SMALL = 307 // 正常通气时，压力传感器数值过小
            const val LOW_SENSOR_PRESSURE = 308 // 传感器压力偏低
            const val TEST_FLOW_TOO_LARGE = 309 // 自检时，流量传感器流速过大
            const val TEST_FLOW_TOO_SMALL = 310 // 自检时，流量传感器流速过小
            const val TEST_FLOW_COMM_ERROR = 311 // 自检时，流量传感器通信异常
            const val TEST_PRESSURE_TOO_LARGE = 312 // 自检时，压力传感器数值过大
            const val TEST_PRESSURE_TOO_SMALL = 313 // 自检时，压力传感器数值过小
            const val TEMP_HUMI_ERROR = 314 // 温/湿度传感器异常
            const val ATMOSPHERIC_SENSOR_ERROR = 315 // 大气压传感器异常
            const val EST_PRESSURE_ERROR = 316 // 预估压力与实际压力相差较远

            //电压
            const val PWR_VOLTAGE_ERROR = 401 // 输入电压异常
            const val PWR_VOLTAGE_TOO_SMALL = 402 // 电源电压低
            const val PWR_VOLTAGE_TOO_LARGE = 403 // 电源电压高

            //其他
            const val EEPROM_ERROR = 501 // EEPROM 只读数据异常
            const val RTC_ERROR = 502 // RTC时钟异常
            const val NEED_TO_CALIBRATE = 503 // 设备需要校准
            const val ABNORMAL_REBOOT = 504 // 设备异常重启

            //事件
            const val EVENT_OA = 601 // 阻塞呼吸暂事件
            const val EVENT_CA = 602 // 中枢型呼吸暂停事件
            const val EVENT_UA = 603 // 无法分类的呼吸暂停事件
            const val EVENT_H = 604 // 低通气
            const val EVENT_RERA = 605 // 微觉醒
            const val EVENT_SNORING = 606 // 打鼾事件
            const val EVENT_PB = 607 // 周期性呼吸事件
            const val EVENT_LL = 608 // 漏气量高事件
            const val EVENT_DROPOFF = 609 // 面罩摘下
            const val EVENT_SPONT = 610 // 自主呼吸占比
        }
    }

    interface VentilatorSystemSetting {
        companion object {
            const val ALL = 0
            const val UNIT = 1 // 单位设置
            const val LANGUAGE = 2 // 语言设置
            const val SCREEN = 3 // 屏幕设置
            const val REPLACEMENT = 4 // 耗材设置
            const val VOLUME = 5 // 音量设置
        }
    }

    interface VentilatorMeasureSetting {
        companion object {
            const val ALL = 0
            const val HUMIDIFICATION = 1 // 湿化等级
            const val PRESSURE_REDUCE = 2 // 呼吸压力释放
            const val AUTO_SWITCH = 3 // 自动启停
            const val PRE_HEAT = 4 // 预加热
            const val RAMP = 5 // 缓慢升压
            const val TUBE_TYPE = 6 // 管道类型
            const val MASK = 7 // 面罩
        }
    }

    interface VentilatorVentilationSetting {
        companion object {
            const val ALL = 0
            const val VENTILATION_MODE = 1 // 通气模式
            const val PRESSURE = 2 // CPAP模式压力
            const val PRESSURE_MAX = 3 // APAP模式压力最大值Pmax
            const val PRESSURE_MIN = 4 // APAP模式压力最小值Pmin
            const val PRESSURE_INHALE = 5 // 吸气压力
            const val PRESSURE_EXHALE = 6 // 呼气压力
            const val INHALE_DURATION = 7 // 吸气时间
            const val RESPIRATORY_RATE = 8 // 呼吸频率
            const val RAISE_DURATION = 9 // 压力上升时间
            const val INHALE_SENSITIVE = 10 // 吸气触发灵敏度
            const val EXHALE_SENSITIVE = 11 // 呼气触发灵敏度
        }
    }

    interface VentilatorWarningSetting {
        companion object {
            const val ALL = 0
            const val LEAK_HIGH = 1 // 漏气量高
            const val LOW_VENTILATION = 2 // 分钟通气量低
            const val VT_LOW = 3 // 潮气量低
            const val RR_HIGH = 4 // 呼吸频率高
            const val RR_LOW = 5 // 呼吸频率低
            const val SPO2_LOW = 6 // 血氧饱和度低
            const val HR_HIGH = 7 // 脉率/心率高
            const val HR_LOW = 8 // 脉率/心率低
            const val APNEA = 9 // 呼吸暂停
        }
    }

    interface VentilatorResponseType {
        companion object {
            const val TYPE_NORMAL_RESPONSE = 1
            const val TYPE_FILE_NOT_FOUND = 224
            const val TYPE_FILE_READ_FAILED = 225
            const val TYPE_FILE_WRITE_FAILED = 226
            const val TYPE_FIRMWARE_UPDATE_FAILED = 227
            const val TYPE_LANGUAGE_UPDATE_FAILED = 228
            const val TYPE_INSUFFICIENT_DATA_LENGTH = 238
            const val TYPE_PARAM_ILLEGAL = 241
            const val TYPE_PERMISSION_DENIED = 242
            const val TYPE_DECRYPT_FAILED = 243
            const val TYPE_DEVICE_BUSY = 251
            const val TYPE_CMD_FORMAT_ERROR = 252
            const val TYPE_CMD_NOT_SUPPORTED = 253
            const val TYPE_NORMAL_ERROR = 255
        }
    }
}