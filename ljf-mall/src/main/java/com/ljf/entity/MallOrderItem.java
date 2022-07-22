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
@TableName("mall_order_item")
@ApiModel(value="MallOrderItem对象", description="")
public class MallOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订单关联购物项主键id")
    @TableId(value = "order_item_id", type = IdType.AUTO)
    private Long orderItemId;

    @ApiModelProperty(value = "订单主键id")
    private Long orderId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long seckillId;

    @ApiModelProperty(value = "关联商品id")
    private Long goodsId;

    @ApiModelProperty(value = "下单时商品的名称(订单快照)")
    private String goodsName;

    @ApiModelProperty(value = "下单时商品的主图(订单快照)")
    private String goodsCoverImg;

    @ApiModelProperty(value = "下单时商品的价格(订单快照)")
    private Integer sellingPrice;

    @ApiModelProperty(value = "数量(订单快照)")
    private Integer goodsCount;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
