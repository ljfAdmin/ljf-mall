package com.ljf.service;

import com.ljf.entity.MallCarousel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallIndexCarouselVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallCarouselService extends IService<MallCarousel> {
    /**
     * 返回固定数量的轮播图对象(首页调用)
     * */
    List<MallIndexCarouselVO> getCarouselsForIndex(int indexCarouselNumber);

}
