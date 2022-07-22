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
 * 
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_seckill")
@ApiModel(value="MallSeckill对象", description="")
public class MallSeckill implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增ID")
    @TableId(value = "seckill_id", type = IdType.AUTO)
    private Long seckillId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "秒杀价格")
    private Integer seckillPrice;

    @ApiModelProperty(value = "秒杀数量")
    private Integer seckillNum;

    @ApiModelProperty(value = "秒杀商品状态（0下架，1上架）")
    private Boolean seckillStatus;

    @ApiModelProperty(value = "秒杀开始时间")
    private Date seckillBegin;

    @ApiModelProperty(value = "秒杀结束时间")
    private Date seckillEnd;

    @ApiModelProperty(value = "秒杀商品排序")
    private Integer seckillRank;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "删除标识字段(0-未删除 1-已删除)")
    private Integer isDeleted;


}
