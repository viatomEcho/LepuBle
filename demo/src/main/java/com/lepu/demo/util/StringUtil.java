package com.lepu.demo.util;

import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static final String CHARSETNAME_UTF_8 = "UTF-8";
	public static final String CHARSETNAME_GBK = "GBK";

	public static boolean stringIsEmpty(String str) {
		return TextUtils.isEmpty(str);
	}

	public static String combinePath(String path1, String path2) {
		String result = null;
		if (stringIsEmpty(path1)) {
			result = path2;
		} else if (stringIsEmpty(path2)) {
			result = path1;
		} else {
			if (!path1.endsWith("/")) {
				result = path1 + "/";
			} else {
				result = path1;
			}
			if (path2.startsWith("/")) {
				result += path2.substring(1);
			} else {
				result += path2;
			}
		}
		return stringIsEmpty(result) ? "" : result;
	}



	// "GB2312"
	public static String bytesToString(byte[] buffer) {
		if (buffer == null) {
			return null;
		}

		String result = "";
		try {
			result = new String(buffer, 0, buffer.length, CHARSETNAME_UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}



	public static boolean isEnglish(String name) {
		String str = "^[a-zA-Z\\s]*$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(name);

		return m.matches();
	}

	public static boolean isChinese(String name) {
		String str = "^[\u4e00-\u9fa5]*$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(name);

		return m.matches();
	}

	public static boolean isPhone(String mobile) {
		String regex = "^1(3[0-9]|4[0-9]|5[0-9]|7[0-9]|8[0-9]|9[0-9])\\d{8}$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(mobile);

		return m.find();
	}

	// 判断是否为汉字
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	// 判断是否为英文
	public static boolean isLetter(String name) {
		String str = "^[a-z0-9A-Z\\s]*$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(name);

		return m.matches();
	}

	public static boolean isHanzi(String name) {
		String str = "^[\u4e00-\u9fa5]*$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(name);

		return m.matches();
	}
}
