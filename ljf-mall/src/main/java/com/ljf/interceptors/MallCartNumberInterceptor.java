package com.ljf.interceptors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.entity.MallShoppingCartItem;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.service.MallShoppingCartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mall购物车数量处理
 * */
@Component
public class MallCartNumberInterceptor implements HandlerInterceptor {
    @Autowired
    private MallShoppingCartItemService mallShoppingCartItemService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 购物车中的数量会更改，但是在部分接口中并没有对session中的数据做修改，这里统一处理一下
        if(null != request.getSession() && null != request.getSession().getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY)){
            // 说明是登录状态，就查询数据库并且设置购物车中的数量值
            MallUserVO mallUserVO = (MallUserVO) request.getSession().getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
            // 设置购物车中的数量
            // 根据用户ID查询购物车中的数据量
            QueryWrapper<MallShoppingCartItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id",mallUserVO.getUserId());
            int count = mallShoppingCartItemService.count(queryWrapper);
            mallUserVO.setShopCartItemCount(count);
            request.getSession().setAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY, mallUserVO);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
