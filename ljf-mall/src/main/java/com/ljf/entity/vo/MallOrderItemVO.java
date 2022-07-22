package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单详情页  页面订单项VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "订单详情页  页面订单项VO")
public class MallOrderItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联商品id")
    private Long goodsId;
    @ApiModelProperty(value = "数量(订单快照)")
    private Integer goodsCount;
    @ApiModelProperty(value = "下单时商品的名称(订单快照)")
    private String goodsName;
    @ApiModelProperty(value = "下单时商品的主图(订单快照)")
    private String goodsCoverImg;
    @ApiModelProperty(value = "下单时商品的价格(订单快照)")
    private Integer sellingPrice;

}
