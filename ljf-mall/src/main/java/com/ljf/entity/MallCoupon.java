package com.ljf.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 优惠券信息及规则表
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_coupon")
@ApiModel(value="MallCoupon对象", description="优惠券信息及规则表")
public class MallCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "coupon_id", type = IdType.AUTO)
    private Long couponId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券介绍，通常是显示优惠券使用限制文字")
    private String couponDesc;

    @ApiModelProperty(value = "优惠券数量，如果是0，则是无限量")
    private Integer couponTotal;

    @ApiModelProperty(value = "优惠金额，")
    private Integer discount;

    @ApiModelProperty(value = "最少消费金额才能使用优惠券。")
    private Integer min;

    @ApiModelProperty(value = "用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.")
    private Integer couponLimit;

    @ApiModelProperty(value = "优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；")
    private Integer couponType;

    @ApiModelProperty(value = "优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。")
    private Integer couponStatus;

    @ApiModelProperty(value = "商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。")
    private Integer goodsType;

    @ApiModelProperty(value = "商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。")
    private String goodsValue;

    @ApiModelProperty(value = "优惠券兑换码")
    private String couponCode;

    @ApiModelProperty(value = "优惠卷开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    private Date couponStartTime;

    @ApiModelProperty(value = "优惠卷结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone="GMT+8")
    private Date couponEndTime;


    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "逻辑删除")
    private Integer isDeleted;


}
