package com.ljf.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MD5Util {
    private MD5Util(){}

    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String getSalt = getSaltT();


    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
    /**
     * 效果等同于md5()和encrypt()方法
     * */
    public static String MD5Encode(String origin, String charsetName) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetName == null || "".equals(charsetName))
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes(charsetName)));
        } catch (Exception exception) {
        }
        return resultString;
    }

    /**
     * 效果等同于encrypt()方法
     * */
    public static String md5(String src){
        return DigestUtils.md5DigestAsHex(src.getBytes());
    }

    /**
     * 效果等同于md5()方法
     * */
    public static String encrypt(String strSrc) {
        try {
            char hexChars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'a', 'b', 'c', 'd', 'e', 'f' };
            byte[] bytes = strSrc.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("MD5加密出错！！+" + e);
        }
    }

    /**
     * 使用前先创建 SecureRandom 对象，并设置加密算法：
     * SecureRandom 默认支持两种加密算法：SHA1PRNG 算法 和 NativePRNG 算法。
     * ① SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
     *  或② SecureRandom random = SecureRandom.getInstance("NativePRNG");
     * 或使用new SecureRandom()来创建 SecureRandom 对象:
     *  ③SecureRandom secureRandom = new SecureRandom();
     *  此时默认会使用 NativePRNG 算法来生成随机数。
     *
     * 不推荐使用SecureRandom.getInstanceStrong()方式获取SecureRandom(除非对随机要求很高)
     *      SecureRandom.getInstanceStrong();依赖操作系统的随机操作
     *  比如键盘输入, 鼠标点击, 等等, 当系统扰动很小时, 产生的随机数不够, 导致读取/dev/random的进程会阻塞等待，
     * 当阻塞时, 多点击鼠标, 键盘输入数据等操作, 会加速结束阻塞
     *
     * 推荐使用new SecureRandom()获取SecureRandom, linux下从/dev/urandom读取. 虽然是伪随机,
     * 但大部分场景下都满足.
     *              SecureRandom secureRandom = new SecureRandom();
     * */
    public static final String getSaltT() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[15];
        random.nextBytes(bytes);// 生成指定数量的随机字节数
        // 使用base64算法对二进制数据进行编码，但不分块输出；返回包含Base64个字符的字符串
        String salt = Base64.encodeBase64String(bytes);
        return salt;
    }

    /**
     * 盐值，随机二次加密
     * */
    public static String formPassFormPass(String inputPass) {
        // String str = "" + getSalt.charAt(0) + getSalt.charAt(2) + inputPass + getSalt.charAt(4) + getSalt.charAt(6);
        // return md5(str);
        return formPassToDBPass(inputPass,getSalt);
    }

    /**
     * 第二次md5--反解密 用户登录验证 ---　salt　可随机
     * */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(4) + salt.charAt(6);
        return md5(str);
    }

    // 测试
    public static void main(String[] args) {
        String s = DigestUtils.md5DigestAsHex("123456".getBytes());
        System.out.println(s);//e10adc3949ba59abbe56e057f20f883e
        System.out.println(s.length());//32

        System.out.println(encrypt("123456"));// e10adc3949ba59abbe56e057f20f883e
        System.out.println(MD5Encode("123456",""));// e10adc3949ba59abbe56e057f20f883e

        System.out.println(getSaltT());// o+gOuh3wboZ0574dK6LH   TRW9h6sb9YdZ5R0tMmip

        System.out.println();
    }
}
