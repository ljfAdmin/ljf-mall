package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品详情页VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "商品详情页VO")
public class MallGoodsInfoDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品表主键id")
    private Long goodsId;
    @ApiModelProperty(value = "商品名")
    private String goodsName;
    @ApiModelProperty(value = "商品简介")
    private String goodsIntro;
    @ApiModelProperty(value = "商品主图")
    private String goodsCoverImg;
    @ApiModelProperty(value = "商品实际售价")
    private Integer sellingPrice;
    @ApiModelProperty(value = "商品价格")
    private Integer originalPrice;
    @ApiModelProperty(value = "商品详情")
    private String goodsDetailContent;

    /**
     * 额外属性
     * */
    @ApiModelProperty(value = "商品首页轮播图列表")
    private String[] goodsCarouselList;
}
