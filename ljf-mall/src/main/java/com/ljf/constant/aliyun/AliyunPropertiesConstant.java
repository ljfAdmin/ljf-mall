package com.ljf.constant.aliyun;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 当项目一启动，spring接口，spring加载之后，就会执行接口的一个方法
 *
 * 一启动项目，该类会交给Spring管理，管理过程中用注解方式将配置文件中的值读取出来，赋值给对应的属性，
 * 当这个过程完成之后，重写的方法就会执行！
 * */
@Component
public class AliyunPropertiesConstant implements InitializingBean {
    // 读取配置文件内容
    @Value("${alipay.app-id}")
    private String appId;
    @Value("${alipay.rsa-private-key}")
    private String rsaPrivateKey;
    @Value("${alipay.alipay-public_key}")
    private String alipayPublicKey;
    @Value("${alipay.gateway}")
    private String gateway;
    @Value("${alipay.charset}")
    private String charset;
    @Value("${alipay.format}")
    private String format;
    @Value("${alipay.log-path}")
    private String logPath;
    @Value("${alipay.sign-type}")
    private String signType;

    /*@Value("${alipay.notify-url}")
    private String notifyUrl;
    @Value("${alipay.return-url}")
    private String returnUrl;*/

    // 定义公开静态常量
    public static String APP_ID;
    public static String RSA_PRIVATE_KEY;
    public static String ALIPAY_PUBLIC_KEY;
    public static String GATEWAY;
    public static String CHARSET;
    public static String FORMAT;
    public static String LOG_PATH;
    public static String SIGN_TYPE;
    /*public static String NOTIFY_URL;
    public static String RETURN_URL;*/

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = this.appId;
        RSA_PRIVATE_KEY = this.rsaPrivateKey;
        ALIPAY_PUBLIC_KEY = this.alipayPublicKey;
        GATEWAY = this.gateway;
        CHARSET = this.charset;
        FORMAT = this.format;
        LOG_PATH = this.logPath;
        SIGN_TYPE = this.signType;
        /*NOTIFY_URL = this.notifyUrl;
        RETURN_URL = this.returnUrl;*/
    }
}
