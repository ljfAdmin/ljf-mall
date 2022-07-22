package com.ljf.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {
    private NumberUtil(){}

    /**
     * 判断是否为11位电话号码
     */
    public static boolean isPhone(String phone) {
        Pattern pattern = Pattern.compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(17[0-8])|(18[0-9]))\\d{8}$");
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    /**
     * 生成指定长度的随机数
     */
    public static int genRandomNum(int length) {
        int num = 1;
        double random = Math.random();// [0,1)
        if (random < 0.1) {// 如果为小于0.1的情况，则保证其大于0.1，保证第一次乘于10的情况下，第一位不为0
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    /**
     * 生成订单流水号
     */
    public static String genOrderNo() {
        StringBuffer buffer = new StringBuffer(String.valueOf(System.currentTimeMillis()));
        int num = genRandomNum(4);
        buffer.append(num);
        return buffer.toString();
    }

}
