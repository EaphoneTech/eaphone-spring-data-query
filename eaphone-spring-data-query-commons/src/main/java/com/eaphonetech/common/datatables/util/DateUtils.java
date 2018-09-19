package com.eaphonetech.common.datatables.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {
    
    public static final String FORMAT_YMD = "yyyy-MM-dd";
    public static final String FORMAT_YMDHMS = "yyyy-MM-dd'T'HH:mm:ss";

    public Date tryParse(String format, String text) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }
}
