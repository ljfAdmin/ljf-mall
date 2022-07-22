package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallCoupon;
import com.ljf.service.MallCouponService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.lettuce.core.resource.KqueueProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
@RequestMapping("/admin")
public class MallCouponController {
    @Autowired
    private MallCouponService mallCouponService;

    /**
     * 在这里更新一下优惠券的显示状态
     * */
    @GetMapping(value = "/coupon")
    public String index(HttpServletRequest request) {
        // 纠正状态！
        /*List<MallCoupon> coupons = mallCouponService.list(null);
        for (MallCoupon coupon : coupons) {
            if(coupon.getCouponStartTime().compareTo(coupon.getCreateTime()) < 0){
                coupon.setCouponStatus(2);
            }else if(coupon.getCouponEndTime().compareTo(new Date()) <= 0){// 非法状态设置为下架
                coupon.setCouponStatus(2);
            }
            mallCouponService.updateById(coupon);
        }*/

        request.setAttribute("path", "ljf_mall_coupon");
        return "admin/mall_coupon";
    }

    /**
     * 分页显示优惠券信息
     * */
    @GetMapping("/coupon/list")
    @ResponseBody
    public Result<Page<MallCoupon>> listCouponsPage(@RequestParam Map<String, Object> params){
        if(params == null || params.size() <= 0
                || StringUtils.isEmpty((CharSequence) params.get("page"))
                || StringUtils.isEmpty((CharSequence) params.get("limit"))){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf(((String) params.get("page")));
        Integer limit = Integer.valueOf(((String) params.get("limit")));
        // Integer start = (currentPage - 1) * limit;

        Page<MallCoupon> page = new Page<>(currentPage,limit);
        QueryWrapper<MallCoupon> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time","update_time");
        // queryWrapper.last("limit "+start+","+limit);
        mallCouponService.page(page,queryWrapper);
        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 新增优惠券信息
     * */
    @PostMapping("/coupon/save")
    @ResponseBody
    public Result saveCoupon(@RequestBody MallCoupon mallCoupon) throws ParseException {
        if(mallCoupon == null || mallCoupon.getCouponStartTime().compareTo(new Date()) < 0
                || mallCoupon.getCouponEndTime().compareTo(mallCoupon.getCouponStartTime()) <= 0)// 或者增加一些其他必要字段值的判断
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());

        /*System.out.println(mallCoupon.getCouponEndTime());
        System.out.println(mallCoupon.getCouponStartTime());
        // 可以写一个工具类
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startFormat = simpleDateFormat.format(mallCoupon.getCouponStartTime());
        String endFormat = simpleDateFormat.format(mallCoupon.getCouponEndTime());
        mallCoupon.setCouponStartTime(new SimpleDateFormat("yyyy-MM-dd").parse(startFormat));
        mallCoupon.setCouponEndTime(new SimpleDateFormat("yyyy-MM-dd").parse(endFormat));*/

        boolean saved = mallCouponService.save(mallCoupon);
        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 更新优惠券信息
     * */
    @ResponseBody
    @PostMapping("/coupon/update")
    public Result updateCoupon(@RequestBody MallCoupon mallCoupon) {
        if(mallCoupon == null)// 或者增加一些其他必要字段值的判断
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());

        boolean updated = mallCouponService.updateById(mallCoupon);

        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 详细信息
     * */
    @GetMapping("/coupon/{id}")
    @ResponseBody
    public Result getCouponInfo(@PathVariable("id") Long id) {
        MallCoupon mallCoupon = mallCouponService.getById(id);
        return mallCoupon == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(mallCoupon);
    }

    /**
     * 删除记录
     * */
    @DeleteMapping("/coupon/{id}")
    @ResponseBody
    public Result deleteCoupon(@PathVariable("id") Long id) {
        boolean deleted = mallCouponService.removeById(id);
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

}

