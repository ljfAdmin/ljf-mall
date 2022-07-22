package com.ljf.service;

import com.ljf.entity.MallUserCouponRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 优惠券用户使用表 服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallUserCouponRecordService extends IService<MallUserCouponRecord> {

    int getUserCouponRecordCount(Long userId, Long couponId);

    int getCouponTotalCount(Long couponId);
}
