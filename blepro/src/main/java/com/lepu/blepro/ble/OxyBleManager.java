package com.lepu.blepro.ble;

import android.content.Context;
import androidx.annotation.NonNull;

import com.lepu.blepro.base.BaseBleManager;
import com.lepu.blepro.utils.LepuBleLog;

public class OxyBleManager extends BaseBleManager {


    public OxyBleManager(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init() {
        LepuBleLog.d("OxyBleManager inited");
    }
}
