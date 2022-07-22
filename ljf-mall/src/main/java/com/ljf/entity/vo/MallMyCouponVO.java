package com.ljf.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MallMyCouponVO implements Serializable {
    private static final long serialVersionUID = -8182785776876066101L;

    /**
     * MallUserCouponRecord类的字段
     * */
    private Long couponUserId;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    /**
     * MallCoupon类的字段
     * */
    @ApiModelProperty(value = "优惠券名称")
    private String couponName;
    @ApiModelProperty(value = "优惠券介绍，通常是显示优惠券使用限制文字")
    private String couponDesc;
    @ApiModelProperty(value = "优惠金额，")
    private Integer discount;
    @ApiModelProperty(value = "最少消费金额才能使用优惠券。")
    private Integer min;
    @ApiModelProperty(value = "商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。")
    private Integer goodsType;
    @ApiModelProperty(value = "商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。")
    private String goodsValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "优惠卷开始时间")
    private Date couponStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "优惠卷结束时间")
    private Date couponEndTime;
}
