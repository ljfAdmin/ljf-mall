package com.ljf.service;

import com.ljf.entity.MallShoppingCartItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallShoppingCartItemVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallShoppingCartItemService extends IService<MallShoppingCartItem> {

    /**
     * 根据用户ID获取属于用户自己的“购物车”VO对象列表
     * */
    List<MallShoppingCartItemVO> getMyShoppingCartItems(Long userId);

    boolean deleteByIdAndUserId(Long mallShoppingCartItemId, Long userId);
}
