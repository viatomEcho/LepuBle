package com.lepu.demo.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Description: <DateUtil><br>
 * Author: mxdl<br>
 * Date: 2018/6/11<br>
 * Version: V1.0.0<br>
 * Update: <br>
 */
public class DateUtil {

    public static final String DATE_ALL_ALL = "yyyy-MM-dd H:mm:ss";
    public static final String YEAR_MONTH_DAY = "yyyy-MM-dd";
    public static final String YEAR_MONTH = "yyyy-MM";
    public static final String DATE_ALL = "yyyy-MM-dd H:mm";
    public static final String DATE_ALL_12 = "yyyy-MM-dd h:mm";
    public static final String DATE_TIME = "MM-dd H:mm";
    public static final String DATE_HOUR_MINUTE = "H:mm";
    public static final String DATE_HOUR_MINUTE_12 = "h:mm";
    public static final String DATE_HOUR_MINUTE_SEC = "H:mm:ss";
    public static final String DATE_HOUR_MINUTE_SEC_12 = "h:mm:ss";
    public static final long WEEK_MILLIS = 604800000L;
    public static final long MONTH_MILLIS = 2592000000L;
    public static final long DAY_MILLIS = 86400000L;
    public static String mCurrentDateFormat;
    private static final long timeZone = TimeZone.getDefault().getRawOffset();
    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };


    public enum FormatType {
        yyyy, yyyyMM, yyyyMMdd, yyyyMMddHHmm, yyyyMMddHHmmss, MMdd, HHmm,MM,dd,MMddHHmm,ddMMyyyy,HHmmss;
    }


    public static String stringFromDate(Date date, String formatString) {
        DateFormat df = new SimpleDateFormat(formatString);
        return df.format(date);
    }
}