package com.lepu.blepro.ble;

import android.content.Context;
import androidx.annotation.NonNull;
import com.lepu.blepro.base.BaseBleManager;
import com.lepu.blepro.ble.cmd.UniversalBleCmd;
import com.lepu.blepro.utils.LepuBleLog;

public class Er1BleManager extends BaseBleManager {

    public Er1BleManager(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init() {
        syncTime();
        getInfo();
        LepuBleLog.d("Er1BleManager inited");

    }

    private void getInfo() {
        sendCmd(UniversalBleCmd.getInfo());
    }

    private void syncTime() {

    }
}
