package com.ljf.interceptors;

import com.alibaba.fastjson.JSONObject;
import com.ljf.annotations.RepeatSubmit;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 重复提交拦截器
 * */
@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {
    /**
     * 请求承诺书
     */
    private static final String REPEAT_PARAMETERS = "repeatParameters";
    /**
     * 请求时间
     */
    private static final String REPEAT_TIME = "repeatTime";
    /**
     * 请求间隔小于10s才处理
     */
    private static final int REPEAT_TIME_INTERVAL = 10;
    /**
     * 请求数据
     * */
    private static final String REPEAT_DATA = "repeatData";

    /**
     *      HandlerMethod封装了很多属性，在访问请求方法的时候可以方便的访问到方法、方法参数、方法上的注解、
     *  所属类等并且对方法参数封装处理，也可以方便的访问到方法参数的注解等信息。
     *      https://blog.csdn.net/x763795151/article/details/88782330
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RepeatSubmit repeatSubmit = handlerMethod.getMethodAnnotation(RepeatSubmit.class);
            if(repeatSubmit == null){
                return true;// 直接放行
            }

            String parametersMap = JSONObject.toJSONString(request.getParameterMap());
            Map<String,Object> nowData = new HashMap<>(8);
            nowData.put(REPEAT_PARAMETERS,parametersMap);
            long nowTime = System.currentTimeMillis();
            nowData.put(REPEAT_TIME,nowTime);

            // URI包括协议、IP和端口号以及URL
            String requestURI = request.getRequestURI();

            HttpSession session = request.getSession();
            Object repeatData = session.getAttribute(REPEAT_DATA);
            if(repeatData != null){
                Map<String, Object> sessionData = (Map<String, Object>) repeatData;
                if (sessionData.containsKey(requestURI)) {
                    Map<String, Object> oldData = (Map<String, Object>) sessionData.get(requestURI);
                    long oldTime = (Long) oldData.get(REPEAT_TIME);
                    String oldParameterMap = (String) oldData.get(REPEAT_PARAMETERS);
                    if(parametersMap.equals(oldParameterMap) && (nowTime - oldTime) / 1000 < REPEAT_TIME_INTERVAL){
                        Result result = ResultGenerator.genFailResult("请勿重复点击");
                        try {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.getWriter().print(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;// 拦截这次请求
                    }
                }
            }

            // repeatData为null
            Map<String, Object> newSessionData = new HashMap<>();
            newSessionData.put(requestURI,nowData);
            session.setAttribute(REPEAT_DATA,newSessionData);

        }
        return true;// 放行
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
