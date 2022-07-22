package com.ljf.service;

import com.ljf.entity.MallGoodsCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallIndexGoodsCategoryVO;
import com.ljf.entity.vo.SearchPageCategoryVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallGoodsCategoryService extends IService<MallGoodsCategory> {
    /**
     * 抽取方法，返回指定某个指定层级的分类下的所有子分类
     * */
    List<MallGoodsCategory> getAppointedLevelGoodsCategories(Long parentId, Integer categoryLevel);

    /**
     * 上述方法的改进
     * */
    List<MallGoodsCategory> getAppointedLevelGoodsCategoriesFromAll(long parentId, Integer categoryLevel, List<MallGoodsCategory> allGoodsCategories);

    List<MallIndexGoodsCategoryVO> getCategoriesForIndex();

    /**
     * 返回分类数据(搜索页调用)
     * */
    SearchPageCategoryVO getCategoriesForSearch(Long categoryId);
}
