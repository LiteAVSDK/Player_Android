package com.tencent.liteav.demo.player.demo.tuishortvideo.tools;

public class Utils {
    /**
     * 将秒数转换为hh:mm:ss的格式
     * @param second 秒
     */
    public static String formattedTime(long second) {
        String formatTime;
        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h == 0) {
            formatTime = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            formatTime = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }
        return formatTime;
    }

    public static String asTwoDigit(long digit) {
        String value = "";
        if (digit < 10) {
            value = "0";
        }
        value += String.valueOf(digit);
        return value;
    }
}
