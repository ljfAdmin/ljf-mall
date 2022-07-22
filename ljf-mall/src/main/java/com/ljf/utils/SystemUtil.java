package com.ljf.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class SystemUtil {
    private SystemUtil(){}

    /**
     * 登录或注册成功后,生成保持用户登录状态会话token值
     *
     * @param src:为用户最新一次登录时的now()+user.id+random(4)
     *
     *
     * https://blog.csdn.net/qfikh/article/details/52832196
     * https://blog.csdn.net/qq_41668547/article/details/87628618
     * 注：
     *   在Java中有两个类BigInteger和BigDecimal分别表示大整数类和大浮点数类，
     * 至于两个类的对象能表示最大范围不清楚，理论上能够表示无线大的数，只要计算机内存足够大。
     * 这两个类都在java.math.*包中；
     *
     *   操作大整数，也可以转换进制。如果在操作的时候一个整型数据已经超过了整数的最大类型长度long的话，
     * 则此数据就无法装入，所以，此时要使用BigInteger类进行操作。这些大数都会以字符串的形式传入。
     *
     *  将BigInteger的符号幅值表示转换为BigInteger。符号表示为整数符号值：-1表示负，0表示零，或1表示正。
     * 幅值是以字节顺序排列的字节数组：最高有效字节位于第0个元素中。允许使用零长度幅值数组，
     * 并且无论signum是-1、0还是1，都将导致BigInteger值为0。
     *
     * 下面方法中的构造方法：
     *   int signum：数字的符号（-1表示负，0表示零，1表示正）
     *   magnitude：数字大小的大端二进制表示法
     * */
    public static String genToken(String src) {
        if (null == src || "".equals(src)) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(src.getBytes());
                String result = new BigInteger(1, md.digest()).toString(16);
            if (result.length() == 31) {
                result = result + "-";
            }
            System.out.println(result);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
