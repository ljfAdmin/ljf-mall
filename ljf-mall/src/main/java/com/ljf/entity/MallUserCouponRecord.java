package com.ljf.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 优惠券用户使用表
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_user_coupon_record")
@ApiModel(value="MallUserCouponRecord对象", description="优惠券用户使用表")
public class MallUserCouponRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "coupon_user_id", type = IdType.AUTO)
    private Long couponUserId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；")
    private Integer useStatus;

    @ApiModelProperty(value = "使用时间")
    private Date usedTime;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    @ApiModelProperty(value = "逻辑删除")
    private Integer isDeleted;


}
