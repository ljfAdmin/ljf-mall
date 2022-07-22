package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.entity.MallCarousel;
import com.ljf.entity.vo.MallIndexCarouselVO;
import com.ljf.mapper.MallCarouselMapper;
import com.ljf.service.MallCarouselService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.utils.BeanUtil;
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
public class MallCarouselServiceImpl extends ServiceImpl<MallCarouselMapper, MallCarousel> implements MallCarouselService {

    /**
     * 返回固定数量的轮播图对象(首页调用)
     * 读多写少，基本不变
     * */
    @Cacheable(value = {"carousel"},key = "#root.methodName",sync = true)
    @Override
    public List<MallIndexCarouselVO> getCarouselsForIndex(int indexCarouselNumber) {
        // System.out.println("进入了getCarouselsForIndex方法");

        List<MallIndexCarouselVO> indexCarouselVOS = new ArrayList<>(indexCarouselNumber);
        // 调用Mapper进行查询
        QueryWrapper<MallCarousel> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("carousel_rank");
        queryWrapper.last("limit "+indexCarouselNumber);
        List<MallCarousel> mallCarousels = baseMapper.selectList(queryWrapper);
        if(!CollectionUtils.isEmpty(mallCarousels)){
            indexCarouselVOS = BeanUtil.copyList(mallCarousels, MallIndexCarouselVO.class);
        }

        return indexCarouselVOS;
    }
}
