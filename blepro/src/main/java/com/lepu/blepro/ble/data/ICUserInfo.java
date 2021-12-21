package com.lepu.blepro.ble.data;

import androidx.annotation.Nullable;

import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithmsPeopleType;
import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithmsSex;
import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithmsType;

public class ICUserInfo implements Cloneable {
    public Integer userIndex = 1;
    public Integer height = 172;
    public double weight = 60.0D;
    public double targetWeight;
    public Integer age = 24;
    public Integer weightDirection;
    public ICBodyFatAlgorithmsType bfaType;
    public ICBodyFatAlgorithmsPeopleType peopleType;
    public ICBodyFatAlgorithmsSex sex;
    public boolean enableMeasureImpendence;
    public boolean enableMeasureHr;
    public boolean enableMeasureBalance;
    public boolean enableMeasureGravity;

    public ICUserInfo() {
        this.sex = ICBodyFatAlgorithmsSex.Male;
        this.peopleType = ICBodyFatAlgorithmsPeopleType.ICBodyFatAlgorithmsPeopleTypeNormal;
        this.bfaType = ICBodyFatAlgorithmsType.ICBodyFatAlgorithmsTypeWLA07;
        this.enableMeasureBalance = true;
        this.enableMeasureGravity = true;
        this.enableMeasureHr = true;
        this.enableMeasureImpendence = true;
        this.targetWeight = 50.0D;
        this.weightDirection = 0;
    }

    public ICUserInfo clone() {
        try {
            return (ICUserInfo)super.clone();
        } catch (CloneNotSupportedException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public Integer getHeight() {
        return this.height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public ICBodyFatAlgorithmsType getBfaType() {
        return this.bfaType;
    }

    public void setBfaType(ICBodyFatAlgorithmsType bfaType) {
        this.bfaType = bfaType;
    }

    public ICBodyFatAlgorithmsPeopleType getPeopleType() {
        return this.peopleType;
    }

    public void setPeopleType(ICBodyFatAlgorithmsPeopleType peopleType) {
        this.peopleType = peopleType;
    }

    public ICBodyFatAlgorithmsSex getSex() {
        return this.sex;
    }

    public void setSex(ICBodyFatAlgorithmsSex sex) {
        this.sex = sex;
    }

    public Integer getUserIndex() {
        return this.userIndex;
    }

    public void setUserIndex(Integer userIndex) {
        this.userIndex = userIndex;
    }

    public boolean isEnableMeasureImpendence() {
        return this.enableMeasureImpendence;
    }

    public void setEnableMeasureImpendence(boolean enableMeasureImpendence) {
        this.enableMeasureImpendence = enableMeasureImpendence;
    }

    public boolean isEnableMeasureHr() {
        return this.enableMeasureHr;
    }

    public void setEnableMeasureHr(boolean enableMeasureHr) {
        this.enableMeasureHr = enableMeasureHr;
    }

    public boolean isEnableMeasureBalance() {
        return this.enableMeasureBalance;
    }

    public void setEnableMeasureBalance(boolean enableMeasureBalance) {
        this.enableMeasureBalance = enableMeasureBalance;
    }

    public boolean isEnableMeasureGravity() {
        return this.enableMeasureGravity;
    }

    public void setEnableMeasureGravity(boolean enableMeasureGravity) {
        this.enableMeasureGravity = enableMeasureGravity;
    }

    public double getTargetWeight() {
        return this.targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public Integer getWeightDirection() {
        return this.weightDirection;
    }

    public void setWeightDirection(Integer weightDirection) {
        this.weightDirection = weightDirection;
    }

    public String toString() {
        return String.format("userIndex:%d,weight:%02f,height:%d,age:%d,sex:%s, weightUnit=%s, rulerUnit=%s,kitchenUnit=%s,bfa=%s, people=%s,rulermode=%s,imp_flag=%s,hr_flag=%s,balance=%s,gravity=%s", this.userIndex, this.weight, this.height, this.age, this.sex, this.bfaType, this.peopleType, this.enableMeasureImpendence, this.enableMeasureHr, this.enableMeasureBalance, this.enableMeasureGravity);
    }

    public boolean equals(@Nullable Object obj) {
        ICUserInfo user = (ICUserInfo)obj;
        if (user.height != this.height) {
            return false;
        } else if (user.weight - this.weight <= 0.001D && user.weight - this.weight >= -0.001D) {
            if (user.age != this.age) {
                return false;
            } else if (user.sex != this.sex) {
                return false;
            } else if (user.peopleType != this.peopleType) {
                return false;
            } else if (user.bfaType != this.bfaType) {
                return false;
            } else if (user.userIndex != this.userIndex) {
                return false;
            } else if (user.enableMeasureHr != this.enableMeasureHr) {
                return false;
            } else if (user.enableMeasureBalance != this.enableMeasureBalance) {
                return false;
            } else if (user.enableMeasureGravity != this.enableMeasureGravity) {
                return false;
            } else {
                return user.enableMeasureImpendence == this.enableMeasureImpendence;
            }
        } else {
            return false;
        }
    }
}
