package com.ljf.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存修改所需要实体
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockNumDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品id")
    private Long goodsId;
    @ApiModelProperty(value = "商品库存数量")
    private Integer goodsCount;
}
