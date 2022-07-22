package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.entity.MallUserCouponRecord;
import com.ljf.mapper.MallUserCouponRecordMapper;
import com.ljf.service.MallUserCouponRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 优惠券用户使用表 服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallUserCouponRecordServiceImpl extends ServiceImpl<MallUserCouponRecordMapper, MallUserCouponRecord> implements MallUserCouponRecordService {

    @Override
    public int getUserCouponRecordCount(Long userId, Long couponId) {
        QueryWrapper<MallUserCouponRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("coupon_id",couponId);
        return baseMapper.selectCount(queryWrapper);
    }

    @Override
    public int getCouponTotalCount(Long couponId) {
        QueryWrapper<MallUserCouponRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coupon_id",couponId);
        return baseMapper.selectCount(queryWrapper);
    }
}
