package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.MallSeckillInfoConstant;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallSeckill;
import com.ljf.redis.MallRedisCache;
import com.ljf.service.MallSeckillService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Api(value = "后台秒杀控制层类")
@Controller
@RequestMapping("/admin")
public class MallSeckillController {
    @Autowired
    private MallSeckillService mallSeckillService;

    @Autowired
    private MallRedisCache mallRedisCache;

    /**
     * 跳转到秒杀页面
     * */
    @ApiOperation(value = "跳转到秒杀页面")
    @GetMapping("/seckill")
    public String toSeckillIndex(HttpServletRequest request) {
        request.setAttribute("path", "ljf_mall_seckill");
        return "admin/mall_seckill";
    }

    /**
     * 分页查询秒杀信息
     * */
    @ApiOperation(value = "条件分页查询秒杀信息")
    @ResponseBody
    @GetMapping("/seckill/list")
    public Result getSeckillListPageWhere(@RequestParam Map<String, Object> params) throws ParseException {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf((String) params.get("page"));
        Integer limit = Integer.valueOf((String) params.get("limit"));
        Page<MallSeckill> page = new Page<>(currentPage,limit);

        QueryWrapper<MallSeckill> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty((String) params.get("goodsId"))){
            queryWrapper.eq("goods_id",Long.valueOf((String) params.get("goodsId")));
        }

        if(!StringUtils.isEmpty((String) params.get("seckillStatus"))){
            queryWrapper.eq("seckill_status",Boolean.valueOf((String) params.get("seckillStatus")));
        }

        if(!StringUtils.isEmpty((String) params.get("startTime"))){
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) params.get("startTime"));
            queryWrapper.gt("create_time",startTime);
        }
        if(!StringUtils.isEmpty((String) params.get("endTime"))){
            Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) params.get("endTime"));
            queryWrapper.gt("create_time",endTime);
        }

        queryWrapper.orderByDesc("create_time","update_time");
        mallSeckillService.page(page,queryWrapper);

        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 秒杀信息的保存
     * */
    @ApiOperation(value = "保存秒杀信息")
    @ResponseBody
    @PostMapping("/seckill/save")
    public Result save(@RequestBody MallSeckill mallSeckill) {
        // 秒杀数量、秒杀商品ID、秒杀价格、
        if (mallSeckill== null || mallSeckill.getGoodsId() < 1
                || mallSeckill.getSeckillNum() < 1
                || mallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.INPUT_PARAM_EXCEPTION.getResult());
        }

        boolean result = mallSeckillService.saveSeckill(mallSeckill);
        if (result) {
            // 虚拟库存预热
            mallRedisCache.setCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + mallSeckill.getSeckillId(), mallSeckill.getSeckillNum());
            // 秒杀商品列表页面缓存删除
            mallRedisCache.deleteObject(MallSeckillInfoConstant.SECKILL_GOODS_LIST);
            return ResultGenerator.genSuccessResult();
        }else{
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult() +" OR "+ ToFrontMessageConstantEnum.SECKILL_GOODS_INFO_NOT_EXIST.getResult());
        }
    }

    /**
     * 更新秒杀订单
     * */
    @ApiOperation(value = "更新秒杀信息")
    @PostMapping("/seckill/update")
    @ResponseBody
    public Result update(@RequestBody MallSeckill mallSeckill) {
        if (mallSeckill == null || mallSeckill.getSeckillId() == null
                || mallSeckill.getGoodsId() < 1
                || mallSeckill.getSeckillNum() < 1
                || mallSeckill.getSeckillPrice() < 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.INPUT_PARAM_EXCEPTION.getResult());
        }

        boolean result = mallSeckillService.updateSeckill(mallSeckill);
        if(result){
            // 虚拟库存预热
            // 秒杀商品库存key前缀   缓存
            mallRedisCache.setCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + mallSeckill.getSeckillId(), mallSeckill.getSeckillNum());
            // 秒杀商品详情页面缓存
            mallRedisCache.deleteObject(MallSeckillInfoConstant.SECKILL_GOODS_DETAIL + mallSeckill.getSeckillId());
            // 秒杀商品列表页面缓存
            mallRedisCache.deleteObject(MallSeckillInfoConstant.SECKILL_GOODS_LIST);
            return ResultGenerator.genSuccessResult();
        }else{
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult() +" OR "+ ToFrontMessageConstantEnum.SECKILL_INFO_OR_GOODS_INFO_NOT_EXIST.getResult());
        }
    }

    /**
     * 秒杀详细信息
     * */
    @ApiOperation(value = "根据Id查询秒杀信息详情")
    @GetMapping("/seckill/{id}")
    @ResponseBody
    public Result getSeckillInfoById(@PathVariable("id") Long id) {
        MallSeckill mallSeckill = mallSeckillService.getById(id);
        return mallSeckill == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(mallSeckill);
    }

    /**
     * 删除相关的秒杀信息
     * */
    @ApiOperation(value = "根据ID主键删除秒杀信息，先删除redis缓存中数据，然后删除数据库中数据")
    @DeleteMapping("/seckill/{id}")
    @ResponseBody
    public Result deleteSeckill(@PathVariable Long id) {
        mallRedisCache.deleteObject(MallSeckillInfoConstant.SECKILL_GOODS_DETAIL + id);
        mallRedisCache.deleteObject(MallSeckillInfoConstant.SECKILL_GOODS_LIST);

        boolean deleted = mallSeckillService.removeById(id);
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }


}

