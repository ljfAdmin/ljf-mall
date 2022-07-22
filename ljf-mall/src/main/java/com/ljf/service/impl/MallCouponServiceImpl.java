package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.entity.MallCoupon;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallUserCouponRecord;
import com.ljf.entity.vo.MallCouponVO;
import com.ljf.entity.vo.MallMyCouponVO;
import com.ljf.entity.vo.MallShoppingCartItemVO;
import com.ljf.mapper.MallCouponMapper;
import com.ljf.mapper.MallUserCouponRecordMapper;
import com.ljf.service.MallCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.service.MallUserCouponRecordService;
import com.ljf.utils.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * <p>
 * 优惠券信息及规则表 服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallCouponServiceImpl extends ServiceImpl<MallCouponMapper, MallCoupon> implements MallCouponService {
    @Autowired
    private MallUserCouponRecordService mallUserCouponRecordService;

    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    /**
     * 获取系统中所有可用的优惠券
     * */
    @Override
    public List<MallCouponVO> selectAvailableCoupon(Long userId) {
        QueryWrapper<MallCoupon> queryWrapper = new QueryWrapper<>();
        // 优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；
        queryWrapper.eq("coupon_type",0);
        // 优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。
        queryWrapper.eq("coupon_status",0);
        List<MallCoupon> mallCoupons = baseMapper.selectList(queryWrapper);

        List<MallCouponVO> mallCouponVOS = BeanUtil.copyList(mallCoupons, MallCouponVO.class);

        for (MallCouponVO mallCouponVO : mallCouponVOS) {
            if(userId != null) {
                int num = mallUserCouponRecordService.getUserCouponRecordCount(userId,mallCouponVO.getCouponId());
                if(num > 0){
                    // 如果查询到的话，则已经接收
                    mallCouponVO.setHasReceived(true);
                }
            }

            if(mallCouponVO.getCouponTotal() != 0){
                int count = mallUserCouponRecordService.getCouponTotalCount(mallCouponVO.getCouponId());
                // 关于id=couponId的记录数已经超过了优惠券数量，设置卖空状态，但是需要注意的是
                // 如果couponTotal的值为0，说明优惠券无总数量限制
                if(mallCouponVO.getCouponTotal() > 0 && count >= mallCouponVO.getCouponTotal()){
                    mallCouponVO.setSaleOut(true);
                }
            }

        }
        return mallCouponVOS;
    }

    /**
     * 获取指定用户的优惠券信息，已经过期的也获取，至于删除与否由用户自己决定
     * */
    @Override
    public List<MallCouponVO> getMyCouponVOs(Long userId) {
        QueryWrapper<MallUserCouponRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.orderByAsc("use_status");
        queryWrapper.orderByDesc("create_time");
        // 通过user_id查询到对应的   用户优惠券使用记录表数据
        // 因为要获得CouponVO对象，必须获得Coupon对象，而通过这个user_id无法获得，只能通过获得使用记录数据
        // 之后才可以获得对应的coupon_id ，进而查询
        List<MallUserCouponRecord> userCouponRecords = mallUserCouponRecordService.list(queryWrapper);
        List<MallCouponVO> mallCouponVOS = new ArrayList<>();

        for (MallUserCouponRecord userCouponRecord : userCouponRecords) {
            MallCoupon coupon = baseMapper.selectById(userCouponRecord.getCouponId());
            MallCouponVO couponVO = new MallCouponVO();
            BeanUtils.copyProperties(coupon,couponVO);
            // 但是couponVO对象还多了属性：saleOut、isUsed、hasReceived、couponUserId（MallUserCouponRecord主键）
            couponVO.setCouponUserId(userCouponRecord.getCouponUserId());
            // getUsedTime：使用时间      设置该优惠券是否被使用过
            couponVO.setUsed(userCouponRecord.getUsedTime() != null);
            // couponVO.setHasReceived(true);

            mallCouponVOS.add(couponVO);
        }
        return mallCouponVOS;
    }

    /**
     * 添加一条优惠券用户使用记录
     *  优惠券使用记录中包括外键：用户ID、优惠券ID、订单ID
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCouponUser(Long couponId, Long userId) {
        MallCoupon coupon = baseMapper.selectById(couponId);
        // CouponLimit:用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.
        if(coupon != null && coupon.getCouponLimit() != 0){// 说明优惠券有数量限制
            int num = mallUserCouponRecordService.getUserCouponRecordCount(userId, couponId);
            if(num >= coupon.getCouponLimit()) {
                // throw new Exception("优惠券已经领够次数了，无法再次领取");
                return false;
            }
        }

        // 优惠券数量，如果是0，则是无限量
        if(coupon.getCouponTotal() != 0){
            int count = mallUserCouponRecordService.getCouponTotalCount(couponId);
            if(count >= coupon.getCouponTotal()){
                // throw new Exception("优惠券已经领完了，无法再次领取");
                return false;
            }
            // 更新优惠券数量
            UpdateWrapper<MallCoupon> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("coupon_total",coupon.getCouponTotal() - 1);
            updateWrapper.eq("coupon_id",couponId);
            updateWrapper.gt("coupon_total",0);
            int updated = baseMapper.update(coupon, updateWrapper);
            if(updated <= 0){
                // throw new Exception("优惠券领取失败!");
                return false;
            }
        }

        MallUserCouponRecord userCouponRecord = new MallUserCouponRecord();
        userCouponRecord.setUserId(userId);
        userCouponRecord.setCouponId(couponId);
        // 新增记录
        return mallUserCouponRecordService.save(userCouponRecord);
    }

    /**
     * 返还优惠券
     * 如何根据orderId操作优惠券相关？
     *      MallUserCouponRecord(couponUserId, userId, couponId, orderId)
     * */
    @Override
    public boolean releaseCoupon(Long orderId) {
        // 一张优惠券只能一个订单使用
        QueryWrapper<MallUserCouponRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderId);
        MallUserCouponRecord mallUserCouponRecord = mallUserCouponRecordService.getOne(queryWrapper);

        if(mallUserCouponRecord == null){
            return false;
        }

        // 恢复优惠券功能
        // @ApiModelProperty(value = "使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；")
        // private Integer useStatus;
        //    @ApiModelProperty(value = "使用时间")
        //    private Date usedTime;
        mallUserCouponRecord.setUseStatus(0);
        // mallUserCouponRecord.setUsedTime(null);

        boolean updated = mallUserCouponRecordService.updateById(mallUserCouponRecord);
        return updated;
    }

    /**
     * 获得可以给新注册用户赠送的优惠券
     * */
    @Override
    public List<MallCoupon> selectAvailableGiveCoupon() {
        QueryWrapper<MallCoupon> queryWrapper = new QueryWrapper<>();
        // 优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；
        // 注：这里仍然可以使用一个枚举将其封装一下
        queryWrapper.eq("coupon_type",1);
        // 优惠券开始时间和结束时间，对于开始时间，没有太大限制，但是这里更应该提供开始时间大于现在时间为好，
        // 但是对于结束时间，必须大于现在时间
        queryWrapper.gt("coupon_end_time",new Date());
        List<MallCoupon> coupons = baseMapper.selectList(queryWrapper);
        return coupons;
    }

    /**
     * 查询订单可以使用的优惠券信息
     * */
    @Override
    public List<MallMyCouponVO> selectOrderCanUseCoupons(List<MallShoppingCartItemVO> myShoppingCartItemVOs, int priceTotal, Long userId) {
        // 获取当前用户可用的优惠券
        QueryWrapper<MallUserCouponRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        // 使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；
        queryWrapper.eq("use_status",0);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit 8");
        List<MallUserCouponRecord> userCouponRecords = mallUserCouponRecordService.list(queryWrapper);

        List<MallMyCouponVO> mallMyCouponVOS = new ArrayList<>();
        if(!CollectionUtils.isEmpty(userCouponRecords)){
            mallMyCouponVOS = BeanUtil.copyList(userCouponRecords, MallMyCouponVO.class);
            List<Long> couponIds = new ArrayList<>();
            for (MallUserCouponRecord userCouponRecord : userCouponRecords) {
                couponIds.add(userCouponRecord.getCouponId());
            }

            // 然后根据这些couponId获取对应的MallCoupon对象，进而封装MallMyCouponVO对象
            List<MallCoupon> coupons = baseMapper.selectBatchIds(couponIds);
            // 如果根据双重循环，复杂度O(n^2)，可以使用HashMap
            Map<Long,MallCoupon> map = new HashMap<>();
            for (MallCoupon coupon : coupons) {
                map.put(coupon.getCouponId(),coupon);
            }

            for (MallMyCouponVO mallMyCouponVO : mallMyCouponVOS) {
                MallCoupon coupon = map.get(mallMyCouponVO.getCouponId());
                if(coupon != null){
                    mallMyCouponVO.setCouponName(coupon.getCouponName());
                    mallMyCouponVO.setCouponDesc(coupon.getCouponDesc());
                    mallMyCouponVO.setDiscount(coupon.getDiscount());
                    mallMyCouponVO.setMin(coupon.getMin());
                    mallMyCouponVO.setGoodsType(coupon.getGoodsType());
                    mallMyCouponVO.setGoodsValue(coupon.getGoodsValue());

                    mallMyCouponVO.setCouponStartTime(coupon.getCouponStartTime());
                    mallMyCouponVO.setCouponEndTime(coupon.getCouponEndTime());

                    // getCouponEndTime:LocalDate类型，但是如果这里是Date类型
                    // ZonedDateTime zonedDateTime = coupon.getCouponEndTime().atStartOfDay(ZoneId.systemDefault());
                    // mallMyCouponVO.setCouponEndTime(Date.from(zonedDateTime.toInstant()));
                }
            }
        }

        for (MallMyCouponVO mallMyCouponVO : mallMyCouponVOS) {
            boolean flag = false;
            // @ApiModelProperty(value = "最少消费金额才能使用优惠券。")
            if(mallMyCouponVO.getMin() <= priceTotal){
                // 商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。
                if(mallMyCouponVO.getGoodsType() == 1){// 指定分类可用
                    // 商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。
                    String[] split = mallMyCouponVO.getGoodsValue().split(",");
                    List<Long> goodsValues = new ArrayList<>(split.length);
                    for (String s : split) {
                        goodsValues.add(Long.valueOf(s));
                    }

                    List<Long> goodsIds = new ArrayList<>();
                    for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOs) {
                        goodsIds.add(myShoppingCartItemVO.getGoodsId());
                    }

                    // 通过goodsId获取所有的商品信息
                    List<MallGoodsInfo> goodsInfos = (List<MallGoodsInfo>) mallGoodsInfoService.listByIds(goodsIds);

                    // 通过商品信息拿到所有的 商品分类ID集合
                    List<Long> categoryIds = new ArrayList<>();
                    for (MallGoodsInfo goodsInfo : goodsInfos) {
                        categoryIds.add(goodsInfo.getGoodsCategoryId());
                    }

                    // 判断goodsValue中指定哪一类别的商品
                    for (Long categoryId : categoryIds) {
                        if(goodsValues.contains(categoryId)){
                            flag = true;
                            break;
                        }
                    }
                }else if (mallMyCouponVO.getGoodsType() == 2) { // 指定商品可用
                    String[] split = mallMyCouponVO.getGoodsValue().split(",");

                    List<Long> goodsValues = new ArrayList<>(split.length);
                    for (String s : split) {
                        goodsValues.add(Long.valueOf(s));
                    }

                    // 这里只需要拿到goodsId即可，判断goodsValues中是否包含
                    List<Long> goodsIds = new ArrayList<>();
                    for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOs) {
                        goodsIds.add(myShoppingCartItemVO.getGoodsId());
                    }

                    for (Long goodsId : goodsIds) {
                        if(goodsValues.contains(goodsId)){
                            flag = true;
                            break;
                        }
                    }
                }else {// 全场通用
                    flag = true;
                }

                // 过滤出来只有flag为true的情况
                if(flag){
                    // 根据discount从大到小排序
                    mallMyCouponVOS.sort(new Comparator<MallMyCouponVO>() {
                        @Override
                        public int compare(MallMyCouponVO o1, MallMyCouponVO o2) {
                            return o1.getDiscount() - o2.getDiscount();
                        }
                    });
                }
            }
        }

        return mallMyCouponVOS;
    }
}
