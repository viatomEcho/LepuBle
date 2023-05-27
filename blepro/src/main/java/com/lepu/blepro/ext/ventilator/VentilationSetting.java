package com.lepu.blepro.ext.ventilator;

public class VentilationSetting {

    private int type;
    private VentilationMode ventilationMode;
    private CpapPressure cpapPressure;
    private ApapPressureMax apapPressureMax;
    private ApapPressureMin apapPressureMin;
    private PressureExhale pressureExhale;
    private PressureInhale pressureInhale;
    private InhaleDuration inhaleDuration;
    private RespiratoryRate respiratoryRate;
    private PressureRaiseDuration pressureRaiseDuration;
    private ExhaleSensitive exhaleSensitive;
    private InhaleSensitive inhaleSensitive;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public VentilationMode getVentilationMode() {
        return ventilationMode;
    }

    public void setVentilationMode(VentilationMode ventilationMode) {
        this.ventilationMode = ventilationMode;
    }

    public CpapPressure getCpapPressure() {
        return cpapPressure;
    }

    public void setCpapPressure(CpapPressure cpapPressure) {
        this.cpapPressure = cpapPressure;
    }

    public ApapPressureMax getApapPressureMax() {
        return apapPressureMax;
    }

    public void setApapPressureMax(ApapPressureMax apapPressureMax) {
        this.apapPressureMax = apapPressureMax;
    }

    public ApapPressureMin getApapPressureMin() {
        return apapPressureMin;
    }

    public void setApapPressureMin(ApapPressureMin apapPressureMin) {
        this.apapPressureMin = apapPressureMin;
    }

    public PressureExhale getPressureExhale() {
        return pressureExhale;
    }

    public void setPressureExhale(PressureExhale pressureExhale) {
        this.pressureExhale = pressureExhale;
    }

    public PressureInhale getPressureInhale() {
        return pressureInhale;
    }

    public void setPressureInhale(PressureInhale pressureInhale) {
        this.pressureInhale = pressureInhale;
    }

    public InhaleDuration getInhaleDuration() {
        return inhaleDuration;
    }

    public void setInhaleDuration(InhaleDuration inhaleDuration) {
        this.inhaleDuration = inhaleDuration;
    }

    public RespiratoryRate getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(RespiratoryRate respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public PressureRaiseDuration getPressureRaiseDuration() {
        return pressureRaiseDuration;
    }

    public void setPressureRaiseDuration(PressureRaiseDuration pressureRaiseDuration) {
        this.pressureRaiseDuration = pressureRaiseDuration;
    }

    public ExhaleSensitive getExhaleSensitive() {
        return exhaleSensitive;
    }

    public void setExhaleSensitive(ExhaleSensitive exhaleSensitive) {
        this.exhaleSensitive = exhaleSensitive;
    }

    public InhaleSensitive getInhaleSensitive() {
        return inhaleSensitive;
    }

    public void setInhaleSensitive(InhaleSensitive inhaleSensitive) {
        this.inhaleSensitive = inhaleSensitive;
    }

    @Override
    public String toString() {
        return "VentilationSetting{" +
                "type=" + type +
                ", ventilationMode=" + ventilationMode +
                ", cpapPressure=" + cpapPressure +
                ", apapPressureMax=" + apapPressureMax +
                ", apapPressureMin=" + apapPressureMin +
                ", pressureExhale=" + pressureExhale +
                ", pressureInhale=" + pressureInhale +
                ", inhaleDuration=" + inhaleDuration +
                ", respiratoryRate=" + respiratoryRate +
                ", pressureRaiseDuration=" + pressureRaiseDuration +
                ", exhaleSensitive=" + exhaleSensitive +
                ", inhaleSensitive=" + inhaleSensitive +
                '}';
    }

    public class VentilationMode {
        private int mode;  // 0:CPAP  1:APAP  2:S   3:S/T   4:T

        public int getMode() {
            return mode;
        }

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public String toString() {
            return "VentilationMode{" +
                    "mode=" + mode +
                    '}';
        }
    }
    // 通气控制：压力
    public class CpapPressure {
        private float pressure;  // CPAP模式压力   默认值:60  步长:5 范围:40-200  单位0.1cmH2O

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        @Override
        public String toString() {
            return "CpapPressure{" +
                    "pressure=" + pressure +
                    '}';
        }
    }
    // 通气控制：最大压力
    public class ApapPressureMax {
        private float max;  // APAP模式压力最大值Pmax 默认值:120 步长:5 范围:Pmin-200  单位0.1cmH2O

        public float getMax() {
            return max;
        }

        public void setMax(float max) {
            this.max = max;
        }

        @Override
        public String toString() {
            return "ApapPressureMax{" +
                    "max=" + max +
                    '}';
        }
    }
    // 通气控制：最小压力
    public class ApapPressureMin {
        private float min;  // APAP模式压力最小值Pmin 默认值:40  步长:5 范围:40-Pmax   单位0.1cmH2O

        public float getMin() {
            return min;
        }

        public void setMin(float min) {
            this.min = min;
        }

        @Override
        public String toString() {
            return "ApapPressureMin{" +
                    "min=" + min +
                    '}';
        }
    }
    // 通气控制：吸气压力
    public class PressureInhale {
        private float inhale;  // 吸气压力 默认值:100  步长:5 范围:40-250   单位0.1cmH2O

        public float getInhale() {
            return inhale;
        }

        public void setInhale(float inhale) {
            this.inhale = inhale;
        }

        @Override
        public String toString() {
            return "PressureInhale{" +
                    "inhale=" + inhale +
                    '}';
        }
    }
    // 通气控制：呼气压力
    public class PressureExhale {
        private float exhale;  // 呼气压力 默认值:60   步长:5 范围:40-250   单位0.1cmH2O

        public float getExhale() {
            return exhale;
        }

        public void setExhale(float exhale) {
            this.exhale = exhale;
        }

        @Override
        public String toString() {
            return "PressureExhale{" +
                    "exhale=" + exhale +
                    '}';
        }
    }
    // 通气控制：吸气时间
    public class InhaleDuration {
        private float duration;  // 吸气时间 默认值:10   步长:1 范围:3-40     单位0.1s

        public float getDuration() {
            return duration;
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "InhaleDuration{" +
                    "duration=" + duration +
                    '}';
        }
    }
    // 通气控制：呼吸频率
    public class RespiratoryRate {
        private int rate;  // 呼吸频率。范围：5-30。 单位/min bpm

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        @Override
        public String toString() {
            return "RespiratoryRate{" +
                    "rate=" + rate +
                    '}';
        }
    }
    // 通气控制：压力上升时间
    public class PressureRaiseDuration {
        private int duration;  // 压力上升时间。范围：100-900ms，步进：50ms

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "PressureRaiseDuration{" +
                    "duration=" + duration +
                    '}';
        }
    }
    // 通气控制：吸气触发灵敏度
    public class InhaleSensitive {
        private int sentive;  // 吸气触发灵敏度Inspiratory Trigger 默认值:3档  范围:0-5档   0:自动档

        public int getSentive() {
            return sentive;
        }

        public void setSentive(int sentive) {
            this.sentive = sentive;
        }

        @Override
        public String toString() {
            return "InhaleSensitive{" +
                    "sentive=" + sentive +
                    '}';
        }
    }
    // 通气控制：呼气触发灵敏度
    public class ExhaleSensitive {
        private int sentive;  // 呼气触发灵敏度Expiratory Trigger  默认值:3档  范围:0-5档   0:自动档

        public int getSentive() {
            return sentive;
        }

        public void setSentive(int sentive) {
            this.sentive = sentive;
        }

        @Override
        public String toString() {
            return "ExhaleSensitive{" +
                    "sentive=" + sentive +
                    '}';
        }
    }
}
