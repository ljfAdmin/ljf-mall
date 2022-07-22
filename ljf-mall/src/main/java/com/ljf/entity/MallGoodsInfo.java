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
@TableName("mall_goods_info")
@ApiModel(value="MallGoodsInfo对象", description="")
public class MallGoodsInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品表主键id")
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Long goodsId;

    @ApiModelProperty(value = "关联分类id")
    private Long goodsCategoryId;


    @ApiModelProperty(value = "商品名")
    private String goodsName;

    @ApiModelProperty(value = "商品简介")
    private String goodsIntro;

    @ApiModelProperty(value = "商品主图")
    private String goodsCoverImg;

    @ApiModelProperty(value = "商品轮播图")
    private String goodsCarousel;

    @ApiModelProperty(value = "商品详情")
    private String goodsDetailContent;

    @ApiModelProperty(value = "商品价格")
    private Integer originalPrice;

    @ApiModelProperty(value = "商品实际售价")
    private Integer sellingPrice;

    @ApiModelProperty(value = "商品库存数量")
    private Integer stockNum;

    @ApiModelProperty(value = "商品标签")
    private String tag;

    @ApiModelProperty(value = "商品上架状态 0-下架 1-上架")
    private Integer goodsSellStatus;



    @ApiModelProperty(value = "添加者主键id")
    private Integer createUser;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "商品添加时间")
    private Date createTime;

    @ApiModelProperty(value = "修改者主键id")
    private Integer updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "商品修改时间")
    private Date updateTime;


}
