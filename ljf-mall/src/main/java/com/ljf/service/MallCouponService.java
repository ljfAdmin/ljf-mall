package com.ljf.service;

import com.ljf.entity.MallCoupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallCouponVO;
import com.ljf.entity.vo.MallMyCouponVO;
import com.ljf.entity.vo.MallShoppingCartItemVO;

import java.util.List;

/**
 * <p>
 * 优惠券信息及规则表 服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallCouponService extends IService<MallCoupon> {

    List<MallCouponVO> selectAvailableCoupon(Long userId);

    List<MallCouponVO> getMyCouponVOs(Long userId);

    boolean saveCouponUser(Long couponId, Long userId);

    boolean releaseCoupon(Long orderId);

    List<MallCoupon> selectAvailableGiveCoupon();

    List<MallMyCouponVO> selectOrderCanUseCoupons(List<MallShoppingCartItemVO> myShoppingCartItemVOs, int priceTotal, Long userId);
}
