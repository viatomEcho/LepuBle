package com.lepu.blepro.ble.cmd;


import com.lepu.blepro.utils.Convertible;

public class VbVibrationSwitcherConfig implements Convertible {
	private int switcher;
	private int hr1;
	private int hr2;


	public boolean getSwitcher() {
		return switcher == 1;
	}

	public void setSwitcher(boolean state) {
		if(state) {
			switcher = 1;
		} else {
			switcher = 0;
		}
	}

	public int getHr1() {
		return hr1;
	}

	public void setHr1(int hr1) {
		this.hr1 = hr1;
	}

	public int getHr2() {
		return hr2;
	}

	public void setHr2(int hr2) {
		this.hr2 = hr2;
	}

	@Override
	public byte[] convert2Data() {
		byte[] data = new byte[3];
		data[0] = (byte) (switcher & 0x01);
		data[1] = (byte) (hr1);
		data[2] = (byte) (hr2);
		return data;
	}

	public static VbVibrationSwitcherConfig parse(byte[] data) {
		VbVibrationSwitcherConfig config = new VbVibrationSwitcherConfig();

		int switcher = data[0] & 0x01;
		boolean state = (switcher == 1);
		config.setSwitcher(state);

		if(data.length == 3) {
			int hr1 = data[1] & 0xFF;
			config.setHr1(hr1);

			int hr2 = data[2] & 0xFF;
			config.setHr2(hr2);
		}

		return config;
	}
}
