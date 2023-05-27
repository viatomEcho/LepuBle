package com.lepu.blepro.ext.ventilator;

public class MeasureSetting {

    private int type;
    private Humidification humidification;
    private PressureReduce pressureReduce;
    private AutoSwitch autoSwitch;
    private PreHeat preHeat;
    private Ramp ramp;
    private TubeType tubeType;
    private Mask mask;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Humidification getHumidification() {
        return humidification;
    }

    public void setHumidification(Humidification humidification) {
        this.humidification = humidification;
    }

    public PressureReduce getPressureReduce() {
        return pressureReduce;
    }

    public void setPressureReduce(PressureReduce pressureReduce) {
        this.pressureReduce = pressureReduce;
    }

    public AutoSwitch getAutoSwitch() {
        return autoSwitch;
    }

    public void setAutoSwitch(AutoSwitch autoSwitch) {
        this.autoSwitch = autoSwitch;
    }

    public PreHeat getPreHeat() {
        return preHeat;
    }

    public void setPreHeat(PreHeat preHeat) {
        this.preHeat = preHeat;
    }

    public Ramp getRamp() {
        return ramp;
    }

    public void setRamp(Ramp ramp) {
        this.ramp = ramp;
    }

    public TubeType getTubeType() {
        return tubeType;
    }

    public void setTubeType(TubeType tubeType) {
        this.tubeType = tubeType;
    }

    public Mask getMask() {
        return mask;
    }

    public void setMask(Mask mask) {
        this.mask = mask;
    }

    @Override
    public String toString() {
        return "MeasureSetting{" +
                "type=" + type +
                ", humidification=" + humidification +
                ", pressureReduce=" + pressureReduce +
                ", autoSwitch=" + autoSwitch +
                ", preHeat=" + preHeat +
                ", ramp=" + ramp +
                ", tubeType=" + tubeType +
                ", mask=" + mask +
                '}';
    }

    // 参数设置：湿化等级
    public class Humidification {
        private int humidification;  // 湿化等级。0：关闭；1-5档；0xff：自动；

        public int getHumidification() {
            return humidification;
        }

        public void setHumidification(int humidification) {
            this.humidification = humidification;
        }

        @Override
        public String toString() {
            return "Humidification{" +
                    "humidification=" + humidification +
                    '}';
        }
    }
    // 参数设置：呼吸压力释放
    public class PressureReduce {
        private int epr;  // 呼气压力释放 0：关闭；1-3档。CPAP，APAP模式下默认值：2

        public int getEpr() {
            return epr;
        }

        public void setEpr(int epr) {
            this.epr = epr;
        }

        @Override
        public String toString() {
            return "PressureReduce{" +
                    "epr=" + epr +
                    '}';
        }
    }
    // 测量设置：自动启停
    public class AutoSwitch {
        private boolean autoStart;  // 自动启动，默认开启
        private boolean autoEnd;    // 自动停止，默认开启

        public boolean isAutoStart() {
            return autoStart;
        }

        public void setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
        }

        public boolean isAutoEnd() {
            return autoEnd;
        }

        public void setAutoEnd(boolean autoEnd) {
            this.autoEnd = autoEnd;
        }

        @Override
        public String toString() {
            return "AutoSwitch{" +
                    "autoStart=" + autoStart +
                    ", autoEnd=" + autoEnd +
                    '}';
        }
    }
    // 测量设置：预加热
    public class PreHeat {
        private boolean on;  // 默认关闭

        public boolean isOn() {
            return on;
        }

        public void setOn(boolean on) {
            this.on = on;
        }

        @Override
        public String toString() {
            return "PreHeat{" +
                    "on=" + on +
                    '}';
        }
    }
    //参数设置：缓冲压力、时间
    public class Ramp {
        private float pressure;   // 缓冲压力 默认值40，步进5。单位：0.1cmH2O
        private int time;  // 缓冲时间 延时， 0-60min，步进5min。默认15min. 0xff 自动

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        @Override
        public String toString() {
            return "Ramp{" +
                    "pressure=" + pressure +
                    ", time=" + time +
                    '}';
        }
    }
    // 设置：管道类型
    public class TubeType {
        private int type;  // 0: 15mm; 1:19mm（显示为22mm）。15（单水平机型）19（双水平机型）

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "TubeType{" +
                    "type=" + type +
                    '}';
        }
    }
    // 设置：面罩
    public class Mask {
        private int type;        // 0: 口鼻罩(full face), 1: 鼻罩(nasal), 2:鼻枕(pillow)
        private float pressure;  // 面罩佩戴匹配测试压力 默认值:100   步长:10 范围:60-180   单位0.1cmH2O

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        @Override
        public String toString() {
            return "Mask{" +
                    "type=" + type +
                    ", pressure=" + pressure +
                    '}';
        }
    }
}