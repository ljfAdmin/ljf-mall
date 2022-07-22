package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 订单列表页面VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "订单列表页面VO")
public class MallOrderListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单表主键id")
    private Long orderId;
    @ApiModelProperty(value = "订单号")
    private String orderNo;
    @ApiModelProperty(value = "订单总价")
    private Integer totalPrice;
    @ApiModelProperty(value = "0.无 1.支付宝支付 2.微信支付")
    private Integer payType;
    @ApiModelProperty(value = "订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭")
    private Integer orderStatus;
    private String orderStatusString;
    @ApiModelProperty(value = "收货人收货地址")
    private String userAddress;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 多出来的属性
     * */
    @ApiModelProperty(value = "订单快照VO列表")
    private List<MallOrderItemVO> mallOrderItemVOS;
    // private String orderStatusString;
}
