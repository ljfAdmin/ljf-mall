package com.ljf.controller.front;


import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallUser;
import com.ljf.entity.vo.MallCouponVO;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.service.MallCouponService;
import com.ljf.service.MallUserCouponRecordService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * <p>
 * 优惠券信息及规则表 前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 *
 * 优惠券信息及规则
 */
@Controller
public class FrontMallCouponController {
    @Autowired
    private MallCouponService mallCouponService;
    @Autowired
    private MallUserCouponRecordService mallUserCouponRecordService;

    /**
     *  跳转到前台coupon列表页面，并查询出所有的优惠券前端VO信息，其
     * 中前端VO信息的一些设置需要判别
     * */
    @GetMapping(value = "/couponList")
    public String toCouponList(HttpServletRequest request, HttpSession session) {
        Long userId = null;
        MallUser mallUser = (MallUser) session.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        if (mallUser != null) {
            userId = mallUser.getUserId();
        }

        List<MallCouponVO> coupons = mallCouponService.selectAvailableCoupon(userId);
        request.setAttribute("coupons", coupons);
        return "mall/coupon-list";
    }

    /**
     * 获取当前用户持有的优惠券
     * */
    @GetMapping("/myCoupons")
    public String myCoupons(HttpServletRequest request, HttpSession session) {
        MallUserVO userVO = (MallUserVO) session.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);

        List<MallCouponVO> coupons = mallCouponService.getMyCouponVOs(userVO.getUserId());
        request.setAttribute("myCoupons", coupons);
        request.setAttribute("path", "myCoupons");
        return "mall/my-coupons";
    }

    /**
     * 提价一条优惠券用户使用记录
     *
     * 实际上就是领取一张优惠券
     * */
    @ResponseBody
    @PostMapping("coupon/{couponId}")
    public Result save(@PathVariable Long couponId, HttpSession session) {
        MallUserVO userVO = (MallUserVO) session.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        boolean saved = mallCouponService.saveCouponUser(couponId, userVO.getUserId());
        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());

    }

    /**
     * 这是  用户优惠券使用表的主键字段
     * */
    @ResponseBody
    @DeleteMapping("coupon/{couponUserId}")
    public Result delete(@PathVariable Long couponUserId) {
        boolean deleted = mallUserCouponRecordService.removeById(couponUserId);
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }


}

