package com.ljf.interceptors;

import com.ljf.constant.FrontMallUserInfoConstant;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mall系统身份验证拦截器
 * */
@Component
public class MallLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 秒杀请求放过（压力测试使用）
        if (request.getRequestURI().startsWith("/seckillExecution")) {
            return true;
        }

        if (null == request.getSession().getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY)) {
            response.sendRedirect( "/login");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
