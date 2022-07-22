package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页分类数据VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "首页分类数据VO")
public class MallIndexGoodsCategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "商品分类id")
    private Long categoryId;
    @ApiModelProperty(value = "分类级别(1-一级分类 2-二级分类 3-三级分类)")
    private Integer categoryLevel;
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    @ApiModelProperty(value = "子分类列表")
    private List<MallIndexSecondLevelCategoryVO> secondLevelCategoryVOS;

}
