package com.lepu.blepro.ext.ventilator;

public class WarningSetting {

    private int type;
    private WarningApnea warningApnea;
    private WarningLeak warningLeak;
    private WarningVt warningVt;
    private WarningVentilation warningVentilation;
    private WarningRrHigh warningRrHigh;
    private WarningRrLow warningRrLow;
    private WarningSpo2Low warningSpo2Low;
    private WarningHrHigh warningHrHigh;
    private WarningHrLow warningHrLow;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public WarningApnea getWarningApnea() {
        return warningApnea;
    }

    public void setWarningApnea(WarningApnea warningApnea) {
        this.warningApnea = warningApnea;
    }

    public WarningLeak getWarningLeak() {
        return warningLeak;
    }

    public void setWarningLeak(WarningLeak warningLeak) {
        this.warningLeak = warningLeak;
    }

    public WarningVt getWarningVt() {
        return warningVt;
    }

    public void setWarningVt(WarningVt warningVt) {
        this.warningVt = warningVt;
    }

    public WarningVentilation getWarningVentilation() {
        return warningVentilation;
    }

    public void setWarningVentilation(WarningVentilation warningVentilation) {
        this.warningVentilation = warningVentilation;
    }

    public WarningRrHigh getWarningRrHigh() {
        return warningRrHigh;
    }

    public void setWarningRrHigh(WarningRrHigh warningRrHigh) {
        this.warningRrHigh = warningRrHigh;
    }

    public WarningRrLow getWarningRrLow() {
        return warningRrLow;
    }

    public void setWarningRrLow(WarningRrLow warningRrLow) {
        this.warningRrLow = warningRrLow;
    }

    public WarningSpo2Low getWarningSpo2Low() {
        return warningSpo2Low;
    }

    public void setWarningSpo2Low(WarningSpo2Low warningSpo2Low) {
        this.warningSpo2Low = warningSpo2Low;
    }

    public WarningHrHigh getWarningHrHigh() {
        return warningHrHigh;
    }

    public void setWarningHrHigh(WarningHrHigh warningHrHigh) {
        this.warningHrHigh = warningHrHigh;
    }

    public WarningHrLow getWarningHrLow() {
        return warningHrLow;
    }

    public void setWarningHrLow(WarningHrLow warningHrLow) {
        this.warningHrLow = warningHrLow;
    }

    @Override
    public String toString() {
        return "WarningSetting{" +
                "type=" + type +
                ", warningApnea=" + warningApnea +
                ", warningLeak=" + warningLeak +
                ", warningVt=" + warningVt +
                ", warningVentilation=" + warningVentilation +
                ", warningRrHigh=" + warningRrHigh +
                ", warningRrLow=" + warningRrLow +
                ", warningSpo2Low=" + warningSpo2Low +
                ", warningHrHigh=" + warningHrHigh +
                ", warningHrLow=" + warningHrLow +
                '}';
    }

    // 报警提示：呼吸暂停
    public class WarningApnea {
        private int apnea;  // 0: Off 范围：10s/20s/30s；级别：高

        public int getApnea() {
            return apnea;
        }

        public void setApnea(int apnea) {
            this.apnea = apnea;
        }

        @Override
        public String toString() {
            return "WarningApnea{" +
                    "apnea=" + apnea +
                    '}';
        }
    }
    // 报警提示：漏气量高
    public class WarningLeak {
        private int high;  // 0: Off 范围：15s/30s/45s/60s； 级别：中

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        @Override
        public String toString() {
            return "WarningLeak{" +
                    "high=" + high +
                    '}';
        }
    }
    // 报警提示：潮气量低
    public class WarningVt {
        private int low;  // 0: Off 范围：200-2000ml；步进：10ml；级别：中

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        @Override
        public String toString() {
            return "WarningVt{" +
                    "low=" + low +
                    '}';
        }
    }
    // 报警提示：分钟通气量低
    public class WarningVentilation {
        private int low;  // 0: off 范围：1-25L/min；步进：1L/min；级别：中

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        @Override
        public String toString() {
            return "WarningVentilation{" +
                    "low=" + low +
                    '}';
        }
    }
    // 报警提示：呼吸频率高
    public class WarningRrHigh {
        private int high;  // 0:Off 范围：1-60bpm；步进：1bpm；级别：中

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        @Override
        public String toString() {
            return "WarningRrHigh{" +
                    "high=" + high +
                    '}';
        }
    }
    // 报警提示：呼吸频率低
    public class WarningRrLow {
        private int low;  // 0:Off 范围：1-60bpm；步进：1bpm；级别：中

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        @Override
        public String toString() {
            return "WarningRrLow{" +
                    "low=" + low +
                    '}';
        }
    }
    // 报警提示：血氧饱和度低
    public class WarningSpo2Low {
        private int low;  // 0:Off 范围：80-95%；步进：1%；级别：中

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        @Override
        public String toString() {
            return "WarningSpo2Low{" +
                    "low=" + low +
                    '}';
        }
    }
    // 报警提示：脉率/心率高
    public class WarningHrHigh {
        private int high;  // 0:Off 范围：100-240bpm；步进：10bpm；级别：中

        public int getHigh() {
            return high;
        }

        public void setHigh(int high) {
            this.high = high;
        }

        @Override
        public String toString() {
            return "WarningHrHigh{" +
                    "high=" + high +
                    '}';
        }
    }
    // 报警提示：脉率/心率低
    public class WarningHrLow {
        private int low;  // 0:Off 范围：30-70bpm；步进：5bpm；级别：中

        public int getLow() {
            return low;
        }

        public void setLow(int low) {
            this.low = low;
        }

        @Override
        public String toString() {
            return "WarningHrLow{" +
                    "low=" + low +
                    '}';
        }
    }
}
