package com.ljf.service;

import com.ljf.entity.MallIndexConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallIndexConfigGoodsVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallIndexConfigService extends IService<MallIndexConfig> {

    /**
     * 返回固定数量的首页配置商品对象(首页调用)
     * */
    List<MallIndexConfigGoodsVO> getConfigGoodsForIndex(Integer configType, int indexGoodsHotNumber);

    /**
     * 返回固定数量的首页配置热门商品对象（首页调用）
     * */
    List<MallIndexConfigGoodsVO> getConfigHotGoodsForIndex();

    /**
     * 返回固定数量的首页配置新上架商品对象（首页调用）
     * */
    List<MallIndexConfigGoodsVO> getConfigNewGoodsForIndex();

    /**
     * 返回固定数量的额首页配置推荐商品对象（首页调用）
     * */
    List<MallIndexConfigGoodsVO> getConfigRecommendGoodsForIndex();

}
