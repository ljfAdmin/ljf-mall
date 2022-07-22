package com.ljf.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpUtil {
    private HttpUtil(){}

    /**
     * 判断当前请求是否是ajax请求
     * return:
     *      true:表示当前请求是ajax请求
     *      false：表示当前请求是普通请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request){
        //1.获取请求消息头
        String acceptHeader = request.getHeader("Accept");
        String xRequestHeader = request.getHeader("X-Request-With");
        //2.进行相应的判断
        return (acceptHeader != null && acceptHeader.contains("application/json")) ||
                (xRequestHeader != null && xRequestHeader.equals("XMLHttpRequest"));
    }
}
