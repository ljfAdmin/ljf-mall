package com.ljf.entity.vo;

import com.ljf.entity.MallGoodsCategory;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索页面分类数据VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "搜索页面分类数据VO")
public class SearchPageCategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstLevelCategoryName;
    private List<MallGoodsCategory> secondLevelCategoryList;
    private String secondLevelCategoryName;
    private List<MallGoodsCategory> thirdLevelCategoryList;

    private String currentCategoryName;

}
