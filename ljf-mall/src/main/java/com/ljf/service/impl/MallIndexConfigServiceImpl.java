package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.FrontIndexConstant;
import com.ljf.constant.enums.MallIndexConfigTypeEnum;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallIndexConfig;
import com.ljf.entity.vo.MallIndexConfigGoodsVO;
import com.ljf.mapper.MallIndexConfigMapper;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.service.MallIndexConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallIndexConfigServiceImpl extends ServiceImpl<MallIndexConfigMapper, MallIndexConfig> implements MallIndexConfigService {
    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    /**
     * 返回固定数量的首页配置商品对象(首页调用)
     *  这里不适合使用缓存，因为根据传入的数据不一致，查询出的结果也不同，可以将其分解为三个方法后使用缓存
     * */
    @Override
    public List<MallIndexConfigGoodsVO> getConfigGoodsForIndex(Integer configType, int indexGoodsHotNumber) {
        List<MallIndexConfigGoodsVO> indexConfigGoodsVOS = new ArrayList<>(indexGoodsHotNumber);
        QueryWrapper<MallIndexConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_type",configType);
        queryWrapper.orderByDesc("config_rank");
        queryWrapper.last("limit "+indexGoodsHotNumber);

        List<MallIndexConfig> mallIndexConfigs = baseMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(mallIndexConfigs)){
            // 取出对应的goods_id
            for (MallIndexConfig mallIndexConfig : mallIndexConfigs) {
                Long goodsId = mallIndexConfig.getGoodsId();
                // 取出对应的商品信息
                MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
                // 将商品信息属性赋值给MallIndexConfigGoodsVO对象
                MallIndexConfigGoodsVO indexConfigGoodsVO = new MallIndexConfigGoodsVO();
                BeanUtils.copyProperties(goodsInfo,indexConfigGoodsVO);
                indexConfigGoodsVOS.add(indexConfigGoodsVO);
            }

            for (MallIndexConfigGoodsVO indexConfigGoodsVO : indexConfigGoodsVOS) {
                String goodsName = indexConfigGoodsVO.getGoodsName();
                String goodsIntro = indexConfigGoodsVO.getGoodsIntro();
                // 字符串过长导致文件超出的问题，直接截取，前端也可以给提示
                if(goodsName.length() > 30){
                    goodsName = goodsName.substring(0, 30) + "...";
                    indexConfigGoodsVO.setGoodsName(goodsName);
                }

                if(goodsIntro.length() > 22){
                    goodsIntro = goodsIntro.substring(0, 22) + "...";
                    indexConfigGoodsVO.setGoodsIntro(goodsIntro);
                }
            }
        }

        return indexConfigGoodsVOS;
    }

    /**
     * 注：按理说，虽然首页商品信息但是对于商品来说，写操作也不算太少，这里的缓存暂且加上！！！！！！！！！
     *      此外，这里仍然可以将下面三个方法中的查询数据库操作减少到一次查询数据库！这里暂且不修改！！！！！
     *
     * 返回固定数量的首页配置热门商品对象（首页调用）
     *  MallIndexConfigTypeEnum.INDEX_HOT_SELLING_GOODS.getConfigType(), FrontIndexConstant.INDEX_GOODS_HOT_NUMBER
     * */
    @Cacheable(value = {"goods"},key = "#root.methodName",sync = true)
    @Override
    public List<MallIndexConfigGoodsVO> getConfigHotGoodsForIndex() {
        System.out.println("进入了getConfigHotGoodsForIndex方法");
        return this.getConfigGoodsForIndex(MallIndexConfigTypeEnum.INDEX_HOT_SELLING_GOODS.getConfigType(), FrontIndexConstant.INDEX_GOODS_HOT_NUMBER);
    }

    /**
     * 返回固定数量的首页配置新上架商品对象（首页调用）
     * MallIndexConfigTypeEnum.INDEX_NEW_GOODS.getConfigType(), FrontIndexConstant.INDEX_GOODS_NEW_NUMBER
     * */
    @Cacheable(value = {"goods"},key = "#root.methodName",sync = true)
    @Override
    public List<MallIndexConfigGoodsVO> getConfigNewGoodsForIndex() {
        System.out.println("进入了getConfigNewGoodsForIndex方法");
        return this.getConfigGoodsForIndex(MallIndexConfigTypeEnum.INDEX_NEW_GOODS.getConfigType(), FrontIndexConstant.INDEX_GOODS_NEW_NUMBER);
    }

    /**
     * 返回固定数量的额首页配置推荐商品对象（首页调用）
     * MallIndexConfigTypeEnum.INDEX_RECOMMEND_FOR_YOU.getConfigType(), FrontIndexConstant.INDEX_GOODS_RECOMMEND_NUMBER
     * */
    @Cacheable(value = {"goods"},key = "#root.methodName",sync = true)
    @Override
    public List<MallIndexConfigGoodsVO> getConfigRecommendGoodsForIndex() {
        System.out.println("进入了getConfigRecommendGoodsForIndex方法");
        return this.getConfigGoodsForIndex(MallIndexConfigTypeEnum.INDEX_RECOMMEND_FOR_YOU.getConfigType(), FrontIndexConstant.INDEX_GOODS_RECOMMEND_NUMBER);
    }


}
