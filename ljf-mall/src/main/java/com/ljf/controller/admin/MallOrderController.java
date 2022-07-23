package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallOrder;
import com.ljf.entity.MallOrderItem;
import com.ljf.entity.vo.MallOrderItemVO;
import com.ljf.service.MallOrderItemService;
import com.ljf.service.MallOrderService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 *
 * 订单表相关
 */
@Api(value = "后台订单信息控制层类")
@Controller
@RequestMapping("/admin")
public class MallOrderController {
    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private MallOrderItemService mallOrderItemService;

    @ApiOperation(value = "跳转到订单页面")
    @GetMapping("/orders")
    public String ordersPage(HttpServletRequest request) {
        request.setAttribute("path", "orders");
        return "admin/mall_order";
    }

    /**
     * 分页条件查询显示列表
     * */
    @ApiOperation(value = "分页条件查询订单信息列表")
    @GetMapping(value = "/orders/list")
    @ResponseBody
    public Result getMallOrderListPageWhere(@RequestParam Map<String, Object> params) throws ParseException {
        if (StringUtils.isEmpty((CharSequence) params.get("page"))
                || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf(((String) params.get("page")));
        Integer limit = Integer.valueOf(((String) params.get("limit")));
        Page<MallOrder> page = new Page<>(currentPage,limit);

        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(((String) params.get("orderNo")))){
            queryWrapper.eq("order_no",(String) params.get("orderNo"));
        }
        if(!StringUtils.isEmpty((String) params.get("userId"))){
            queryWrapper.eq("user_id",Long.valueOf((String) params.get("userId")));
        }
        if(!StringUtils.isEmpty((String) params.get("payType"))){
            queryWrapper.eq("pay_type",Integer.valueOf((String) params.get("payType")));
        }
        if(!StringUtils.isEmpty((String) params.get("orderStatus"))){
            queryWrapper.eq("order_status",Integer.valueOf((String) params.get("orderStatus")));
        }
        if(!StringUtils.isEmpty((String) params.get("isDeleted"))){
            queryWrapper.eq("is_deleted",Integer.valueOf((String) params.get("isDeleted")));
        }
        if(!StringUtils.isEmpty((String) params.get("startTime"))){
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) params.get("startTime"));
            queryWrapper.gt("create_time",startTime);
        }
        if(!StringUtils.isEmpty((String) params.get("endTime"))){
            Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse((String) params.get("endTime"));
            queryWrapper.gt("create_time",endTime);
        }
        queryWrapper.orderByDesc("create_time");

        mallOrderService.page(page,queryWrapper);

        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 修改
     */
    @ApiOperation(value = "修改订单信息")
    @PostMapping(value = "/orders/update")
    @ResponseBody
    public Result updateMallOrder(@RequestBody MallOrder order) {
        if (Objects.isNull(order.getTotalPrice())
                || Objects.isNull(order.getOrderId())
                || order.getOrderId() < 1
                || order.getTotalPrice() < 1
                || StringUtils.isEmpty(order.getUserAddress())) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean updated = mallOrderService.updateById(order);
        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 详情
     * */
    @ApiOperation(value = "根据主键ID查询订单详情")
    @GetMapping(value = "/order-items/{id}")
    @ResponseBody
    public Result getMallOrderById(@PathVariable("id") Long id){
        MallOrder order = mallOrderService.getById(id);

        List<MallOrderItemVO> orderItemVOS = new ArrayList<>();
        if(order != null){
            QueryWrapper<MallOrderItem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_id",order.getOrderId());
            List<MallOrderItem> orderItems = mallOrderItemService.list(queryWrapper);
            // 获取订单项数据
            if(!CollectionUtils.isEmpty(orderItems)){
                for (MallOrderItem orderItem : orderItems) {
                    MallOrderItemVO mallOrderItemVO = new MallOrderItemVO();
                    BeanUtils.copyProperties(orderItem,mallOrderItemVO);
                    orderItemVOS.add(mallOrderItemVO);
                }
            }
        }

        if (!CollectionUtils.isEmpty(orderItemVOS)) {
            return ResultGenerator.genSuccessResult(orderItemVOS);
        }

        return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DATA_NOT_EXIST.getResult());
    }

    /**
     * 检查订单是否完成，并关闭
     *  @ApiModelProperty(value = "订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭")
     *     private Integer orderStatus;
     * */
    @ApiOperation(value = "检查订单是否完成，并商家关闭")
    @PostMapping(value = "/orders/checkDone")
    @ResponseBody
    public Result checkDone(@RequestBody Long[] ids){
        if(ids == null || ids.length < 1){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        /**
         * 检查订单是否完成，并关闭
         * */
        String checkDone = mallOrderService.checkDone(ids);
        return ToFrontMessageConstantEnum.SUCCESS.getResult().equals(checkDone) ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(checkDone);
    }

    /**
     * 出库，并修改状态为已出库
     * */
    @ApiOperation(value = "出库，并修改状态为“出库成功”")
    @PostMapping(value = "/orders/checkOut")
    @ResponseBody
    public Result checkOut(@RequestBody Long[] ids){
        if(ids == null || ids.length < 1){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        String checkOut = mallOrderService.checkOut(ids);
        return ToFrontMessageConstantEnum.SUCCESS.getResult().equals(checkOut) ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(checkOut);
    }

    /**
     * 关闭订单
     * */
    @ApiOperation(value = "根据主键数组批量关闭订单，订单状态变为商家关闭")
    @PostMapping(value = "/orders/close")
    @ResponseBody
    public Result closeOrder(@RequestBody Long[] ids) {
        if (ids == null || ids.length < 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        String closeOrder = mallOrderService.closeOrder(ids);
        return ToFrontMessageConstantEnum.SUCCESS.getResult().equals(closeOrder) ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(closeOrder);
    }

}

