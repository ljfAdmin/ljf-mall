package com.ljf.interceptors;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 后台系统身份验证拦截器
 * */
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    /**
     * 注意：只拦截后台的请求
     * */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();// /admin/

        if(servletPath.startsWith("/admin") && null == request.getSession().getAttribute("loginUser")){
            // 需要进行后台管理员登录
            request.getSession().setAttribute("errorMsg", "请登陆");
            response.sendRedirect( "/admin/login");
            return false;
        }else{
            request.getSession().removeAttribute("errorMsg");
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
