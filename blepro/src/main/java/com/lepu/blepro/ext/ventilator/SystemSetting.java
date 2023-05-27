package com.lepu.blepro.ext.ventilator;

public class SystemSetting {

    private int type = 0;
    private UnitSetting unitSetting;
    private LanguageSetting languageSetting;
    private ScreenSetting screenSetting;
    private Replacements replacements;
    private VolumeSetting volumeSetting;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UnitSetting getUnitSetting() {
        return unitSetting;
    }

    public void setUnitSetting(UnitSetting unitSetting) {
        this.unitSetting = unitSetting;
    }

    public LanguageSetting getLanguageSetting() {
        return languageSetting;
    }

    public void setLanguageSetting(LanguageSetting languageSetting) {
        this.languageSetting = languageSetting;
    }

    public ScreenSetting getScreenSetting() {
        return screenSetting;
    }

    public void setScreenSetting(ScreenSetting screenSetting) {
        this.screenSetting = screenSetting;
    }

    public Replacements getReplacements() {
        return replacements;
    }

    public void setReplacements(Replacements replacements) {
        this.replacements = replacements;
    }

    public VolumeSetting getVolumeSetting() {
        return volumeSetting;
    }

    public void setVolumeSetting(VolumeSetting volumeSetting) {
        this.volumeSetting = volumeSetting;
    }

    @Override
    public String toString() {
        return "SystemSetting{" +
                "type=" + type +
                ", unitSetting=" + unitSetting +
                ", languageSetting=" + languageSetting +
                ", screenSetting=" + screenSetting +
                ", replacements=" + replacements +
                ", volumeSetting=" + volumeSetting +
                '}';
    }

    public class UnitSetting {
        private int pressureUnit;  // 0: cmH2O; 1: hPa

        public int getPressureUnit() {
            return pressureUnit;
        }

        public void setPressureUnit(int pressureUnit) {
            this.pressureUnit = pressureUnit;
        }

        @Override
        public String toString() {
            return "UnitSetting{" +
                    "pressureUnit=" + pressureUnit +
                    '}';
        }
    }
    public class LanguageSetting {
        private int language;  // 0: 英语; 1: 中文

        public int getLanguage() {
            return language;
        }

        public void setLanguage(int language) {
            this.language = language;
        }

        @Override
        public String toString() {
            return "LanguageSetting{" +
                    "language=" + language +
                    '}';
        }
    }
    public class ScreenSetting {
        private int brightness;  // 屏幕亮度。5-100%，步进1%。默认60%
        private int autoOff;     // 自动息屏。0:常亮。其他有效值：30，60，90，120. 单位秒。默认30秒

        public int getBrightness() {
            return brightness;
        }

        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        public int getAutoOff() {
            return autoOff;
        }

        public void setAutoOff(int autoOff) {
            this.autoOff = autoOff;
        }

        @Override
        public String toString() {
            return "ScreenSetting{" +
                    "brightness=" + brightness +
                    ", autoOff=" + autoOff +
                    '}';
        }
    }
    public class Replacements {
        private int filter;  // 过滤棉。0：关闭；其他有效值1-12，单位月。默认1.
        private int mask;    // 面罩。0：关闭；其他有效值1-12，单位月。默认3.
        private int tube;    // 管路。0：关闭；其他有效值1-12，单位月。默认3.
        private int tank;    // 水箱。0：关闭；其他有效值1-12，单位月。默认6.

        public int getFilter() {
            return filter;
        }

        public void setFilter(int filter) {
            this.filter = filter;
        }

        public int getMask() {
            return mask;
        }

        public void setMask(int mask) {
            this.mask = mask;
        }

        public int getTube() {
            return tube;
        }

        public void setTube(int tube) {
            this.tube = tube;
        }

        public int getTank() {
            return tank;
        }

        public void setTank(int tank) {
            this.tank = tank;
        }

        @Override
        public String toString() {
            return "Replacements{" +
                    "filter=" + filter +
                    ", mask=" + mask +
                    ", tube=" + tube +
                    ", tank=" + tank +
                    '}';
        }
    }
    public class VolumeSetting {
        private int volume;  // 音量, 0-100%，步进1%；默认30%

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }

        @Override
        public String toString() {
            return "VolumeSetting{" +
                    "volume=" + volume +
                    '}';
        }
    }
}
