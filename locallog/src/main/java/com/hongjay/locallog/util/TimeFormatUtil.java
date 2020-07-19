package com.hongjay.locallog.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 类描述： 时间格式化工具类
 * 创建时间：2018/8/30 14:48.
 */

public class TimeFormatUtil {

    public static final String FORMAT_MINUTE = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_DATE = "yyyy-MM-dd";

    /**
     * 格式化常用的时间格式
     *
     * @param time
     * @return
     */
    public static String commFormat(long time) {
        SimpleDateFormat formatComm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        if (time <= 0) {
            return "";
        }
        return formatComm.format(new Date(String.valueOf(time).length() == 13 ? time : time * 1000));
    }

    /**
     * 格式化年月日的时间格式
     *
     * @param time
     * @return
     */
    public static String dateFormat(long time) {
        SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        if (time <= 0) {
            return "";
        }
        return FORMAT_DATE.format(new Date(String.valueOf(time).length() == 13 ? time : time * 1000));
    }

    /**
     * String转long
     *
     * @param time
     * @return
     */
    public static long String2Long(String time) {
        try {
            SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Date date = new Date();
            date = FORMAT_DATE.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * 格式化时分的时间格式
     *
     * @param time
     * @return
     */
    public static String timeFormat(long time) {
        SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:SS", Locale.CHINA);
        if (time <= 0) {
            return "";
        }
        return FORMAT_TIME.format(new Date(String.valueOf(time).length() == 13 ? time : time * 1000));
    }

    //String转换为Calendar时间
    public static Calendar getStrCalendar(String time, String format) {
        Calendar theCa = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date today = sdf.parse(time);
            theCa.setTime(today);
            return theCa;
        } catch (ParseException e) {
            return theCa;
        }
    }


    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }


    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }


    /**
     * 得到本日0点
     */
    public static String getDayFirst() {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        date = calendar.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(date) + " 00:00:00";
    }


    /**
     * 得到本日23点59分59秒
     */
    public static String getDayEnd() {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        date = calendar.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(date) + " 23:59:59";
    }

    /**
     * 通过时间秒毫秒数判断两个时间的间隔(天数)
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int differentDaysByMillisecond(String time1, String time2) {
        Date date1 = string2Unix(time1, "yyyy-MM-dd HH:mm:ss");
        Date date2 = string2Unix(time2, "yyyy-MM-dd HH:mm:ss");
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }

    /**
     * String to unix
     */
    public static Date string2Unix(String time, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    //返回近7天的开始时间
    public static String get7Day() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -6);//前一天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        return date;
    }

    //返回近3天的开始时间
    public static String get3Day() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -2);//前一天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        return date;
    }

    //返回昨天的时间
    public static String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -1);//前一天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        return date;
    }


    /**
     * 获取N天前的时间
     *
     * @param n
     * @return
     */
    public static String getNday(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, n);//前一天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        return date;
    }

    /**
     * 获取N天前的时间
     *
     * @param n
     * @return
     */
    public static String getPreNDay(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, n * -1);//前一天
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(calendar.getTime());
        return date;
    }

    /**
     * 获取当前时间
     * return format对应的时间格式
     */
    public static String getTimeStamp(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        return str != null ? str : "";
    }

    /**
     * 获取两个时间间隔
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        String htmlString = "";
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = nowDate.getTime() - endDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        if (day == 0 && hour == 0 && min == 0) {
            htmlString = "少于1分钟";
        } else if (day == 0 && hour == 0 && min > 0) {
            htmlString = "历时<font color=\"#332410\"><big>" + min + "</big></font>分钟";
        } else if (day == 0 && hour > 0 && min > 0) {
            htmlString = "历时<font color=\"#332410\"><big>" + hour + "</big></font>小时" +
                    "<font color=\"#332410\"><big>" + min + "</big></font>分钟";
        } else {
            htmlString = "历时<font color=\"#332410\"><big>" + day + "</big></font>天" +
                    "<font color=\"#332410\"><big>" + hour + "</big></font>小时" +
                    "<font color=\"#332410\"><big>" + min + "</big></font>分钟";
        }

        return htmlString;
    }

}
