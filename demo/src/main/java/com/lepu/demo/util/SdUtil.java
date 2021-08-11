package com.lepu.demo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author wxd
 */
public class SdUtil {

	public static int STORGE_VALUE = 300;// 400MB

	@SuppressLint("NewApi")
	public static boolean checkSdCanUse() {
		boolean flag = false;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			flag = true;
		}
		return flag;
	}

	public static boolean checkReadWrite(File file) {
		boolean isYes = false;
		if (file != null && file.canRead() && file.canWrite()) {
			isYes = true;
		}
		return isYes;
	}

	/**
	 * 检查是否安装外置的SD卡
	 */
	private static boolean checkExternalSDExists() {
		Map<String, String> evn = System.getenv();
		return evn.containsKey("SECONDARY_STORAGE");
	}

	/**
	 * 获取手机 外置SD卡的根目录
	 * 
	 */
	public static String getExternalSDRoot() {
		String sdRootPath = "";
		if (checkExternalSDExists()) {
			Map<String, String> evn = System.getenv();
			sdRootPath = evn.get("SECONDARY_STORAGE");
		}
		return sdRootPath;
	}

	/**
	 * 获取存储路径
	 * 
	 * 优先获取外部的，如果不存在就取内部的存储空间
	 */
	@SuppressLint("InlinedApi")
	public static String getStorgePath(Context context) {

		boolean flag = false;
		String path = null;
		if (checkSdCanUse()) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath();

		} else {
			flag = true;
		}

		if (flag) {
			StorageManager sm = (StorageManager) context
					.getSystemService(Context.STORAGE_SERVICE);
			try {
				Method method = sm.getClass().getMethod("getVolumePaths", new Class<?>[]{});
				Object object = method.invoke(sm, new Object[]{});
				String[] paths = (String[]) object;
				if (paths != null && paths.length > 0) {
					for (int i = 0; i < paths.length; i++) {
						File file = new File(paths[i]);
						if (checkReadWrite(file)) {
							path = paths[i];
						}
					}
				}
			} catch (Exception e) {
				Log.e("SDUtil",Log.getStackTraceString(e));
			}
		}

		return path;
	}




}
