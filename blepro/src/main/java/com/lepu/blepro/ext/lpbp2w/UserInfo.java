package com.lepu.blepro.ext.lpbp2w;

public class UserInfo {
    private int aid = 0;                // 主账户id
    private int uid = 0;                // 用户id
    private String firstName = "";      // 姓
    private String lastName = "";       // 名
    private String birthday = "0-0-0";  // 出生日期
    private int height = 0;             // 身高 cm (init 170cm -> cmdSend 1700)
    private float weight = 0f;          // 体重 kg (init 75.5kg -> cmdSend 755)
    private int gender = 0;             // 性别 0：男 1：女
    private Icon icon;

    public int getAid() {
        return aid;
    }

    public void setAid(int aid) {
        this.aid = aid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "aid=" + aid +
                ", uid=" + uid +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", gender=" + gender +
                '}';
    }

    public class Icon {
        private int width;
        private int height;
        private byte[] icon;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public byte[] getIcon() {
            return icon;
        }

        public void setIcon(byte[] icon) {
            this.icon = icon;
        }
    }
}
