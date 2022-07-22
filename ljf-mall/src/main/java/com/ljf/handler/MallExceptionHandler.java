package com.ljf.handler;

import com.ljf.utils.HttpUtil;
import com.ljf.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 * */
@RestControllerAdvice
public class MallExceptionHandler {
    Logger log = LoggerFactory.getLogger(MallExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest req) {
        Result result = new Result();
        result.setResultCode(500);
        // 区分是否为自定义异常
        /*if (e instanceof CustomMallException) {
            result.setMessage(e.getMessage());
        } else {
            log.error(e.getMessage(), e);
            result.setMessage("未知异常");
        }*/
        log.error(e.getMessage(),e);
        result.setMessage("未知异常");

        if (HttpUtil.isAjaxRequest(req)) { // 是AJAX请求
            return result;
        }

        // 普通请求
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("message", e.getMessage());
        modelAndView.addObject("url", req.getRequestURL());
        modelAndView.addObject("stackTrace", e.getStackTrace());
        modelAndView.addObject("author", "ljf");
        modelAndView.addObject("ltd", "ljf商城");
        modelAndView.setViewName("error/error");
        return modelAndView;
    }
}
