package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallCoupon;
import com.ljf.service.MallCouponService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;
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
@Api(value = "后台优惠券信息控制层类")
@Controller
@RequestMapping("/admin")
public class MallCouponController {
    @Autowired
    private MallCouponService mallCouponService;

    /**
     * 在这里更新一下优惠券的显示状态
     * */
    @ApiOperation(value = "跳转到优惠券页面")
    @GetMapping(value = "/coupon")
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "ljf_mall_coupon");
        return "admin/mall_coupon";
    }

    /**
     * 分页显示优惠券信息
     * */
    @ApiOperation(value = "分页查询优惠券信息")
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
    @ApiOperation(value = "新增优惠券信息")
    @PostMapping("/coupon/save")
    @ResponseBody
    public Result saveCoupon(@RequestBody MallCoupon mallCoupon) throws ParseException {
        if(mallCoupon == null || mallCoupon.getCouponStartTime().compareTo(new Date()) < 0
                || mallCoupon.getCouponEndTime().compareTo(mallCoupon.getCouponStartTime()) <= 0)// 或者增加一些其他必要字段值的判断
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());

        boolean saved = mallCouponService.save(mallCoupon);
        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 更新优惠券信息
     * */
    @ApiOperation(value = "更新优惠券信息")
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
    @ApiOperation(value = "根据主键ID查询优惠券信息")
    @GetMapping("/coupon/{id}")
    @ResponseBody
    public Result getCouponInfo(@PathVariable("id") Long id) {
        MallCoupon mallCoupon = mallCouponService.getById(id);
        return mallCoupon == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(mallCoupon);
    }

    /**
     * 删除记录
     * */
    @ApiOperation(value = "根据主键ID删除优惠券信息")
    @DeleteMapping("/coupon/{id}")
    @ResponseBody
    public Result deleteCoupon(@PathVariable("id") Long id) {
        boolean deleted = mallCouponService.removeById(id);
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

}

