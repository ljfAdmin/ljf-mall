package com.ljf.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MallSeckillGoodsVO implements Serializable {
    private static final long serialVersionUID = -8719192110998138980L;

    @ApiModelProperty(value = "自增ID")
    private Long seckillId;
    @ApiModelProperty(value = "秒杀商品ID")
    private Long goodsId;
    @ApiModelProperty(value = "秒杀商品名称")
    private String goodsName;
    @ApiModelProperty(value = "秒杀商品介绍")
    private String goodsIntro;
    @ApiModelProperty(value = "商品详情")
    private String goodsDetailContent;
    @ApiModelProperty(value = "商品主图")
    private String goodsCoverImg;
    @ApiModelProperty(value = "商品实际售价")
    private Integer sellingPrice;
    @ApiModelProperty(value = "秒杀价格")
    private Integer seckillPrice;
    @ApiModelProperty(value = "秒杀开始时间")
    private Date seckillBegin;
    @ApiModelProperty(value = "秒杀结束时间")
    private Date seckillEnd;

    /**
     * 额外的字段
     * */
    private String seckillBeginTime;
    private String seckillEndTime;
    private Long startDate;
    private Long endDate;
}
