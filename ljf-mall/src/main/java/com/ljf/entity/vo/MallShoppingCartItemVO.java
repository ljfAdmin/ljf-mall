package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 购物车页面购物项VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "购物车页面购物项VO")
public class MallShoppingCartItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "购物项主键id")
    private Long cartItemId;
    @ApiModelProperty(value = "关联商品id")
    private Long goodsId;
    @ApiModelProperty(value = "购物车中这种商品的数量(最大为5)")
    private Integer goodsCount;

    /**
     * 以下三个属性是MallGoodsInfo实体类中的
     * */
    @ApiModelProperty(value = "商品名")
    private String goodsName;
    @ApiModelProperty(value = "商品主图")
    private String goodsCoverImg;
    @ApiModelProperty(value = "商品实际售价")
    private Integer sellingPrice;

}
