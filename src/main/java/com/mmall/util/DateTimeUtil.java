package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * Created by apple on 2018/6/25.
 */
public class DateTimeUtil {

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String dateToString(Date date, String dateFormat) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime();
        return dateTime.toString(dateFormat);
    }

    public static Date stringToDate(String str, String dateFormat) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(dateFormat);
        DateTime dateTime = dateTimeFormat.parseDateTime(str);
        return dateTime.toDate();
    }

    public static String dateToString(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime();
        return dateTime.toString(STANDARD_FORMAT);
    }

    public static Date stringToDate(String str) {
        DateTimeFormatter dateTimeFormat = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormat.parseDateTime(str);
        return dateTime.toDate();
    }


}
