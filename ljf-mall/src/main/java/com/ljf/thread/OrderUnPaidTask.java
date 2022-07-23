package com.ljf.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.enums.MallOrderStatusEnum;
import com.ljf.constant.MallSeckillInfoConstant;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallOrder;
import com.ljf.entity.MallOrderItem;
import com.ljf.entity.MallSeckill;
import com.ljf.redis.MallRedisCache;
import com.ljf.service.*;
import com.ljf.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class OrderUnPaidTask extends Task {
    // 获取日志记录对象
    private final Logger log = LoggerFactory.getLogger(OrderUnPaidTask.class);

    /**
     * 默认延迟时间30分钟，单位毫秒
     */
    private static final long DELAY_TIME = 30 * 60 * 1000;

    /**
     * 订单id
     */
    private final Long orderId;

    /**
     * @param orderId                  定时任务ID
     * @param delayInMilliseconds 延迟执行时间，单位/毫秒
     */
    public OrderUnPaidTask(Long orderId, long delayInMilliseconds) {
        super("OrderUnPaidTask-" + orderId, delayInMilliseconds);
        this.orderId = orderId;
    }

    public OrderUnPaidTask(Long orderId) {
        super("OrderUnPaidTask-" + orderId, DELAY_TIME);
        this.orderId = orderId;
    }


    /**
     * 进入这个方法 说明任务已经超时，需要进行一些列类似于回滚的操作
     */
    @Override
    public void run() {
        log.info("系统开始处理延时任务---订单超时未付款--- {}", this.orderId);

        MallOrderService mallOrderService = SpringContextUtil.getBean(MallOrderService.class);
        MallOrderItemService mallOrderItemService = SpringContextUtil.getBean(MallOrderItemService.class);
        MallGoodsInfoService mallGoodsInfoService = SpringContextUtil.getBean(MallGoodsInfoService.class);
        MallCouponService mallCouponService = SpringContextUtil.getBean(MallCouponService.class);
        MallSeckillService mallSeckillService = SpringContextUtil.getBean(MallSeckillService.class);
        MallRedisCache mallRedisCache = SpringContextUtil.getBean(MallRedisCache.class);

        MallOrder mallOrder = mallOrderService.getById(this.orderId);

        if (mallOrder == null) {
            // 按照 INFO 级别打印日志
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }
        // 待支付
        if (MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus().equals(mallOrder.getOrderStatus())) {
            log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
            return;
        }

        // 设置订单为已取消状态    超时关闭
        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());

        boolean updated = mallOrderService.updateById(mallOrder);
        if (!updated) {
            throw new RuntimeException("更新数据已失效");
        }

        // 商品货品数量增加
        QueryWrapper<MallOrderItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",this.orderId);
        List<MallOrderItem> orderItems = mallOrderItemService.list(queryWrapper);
        if(!CollectionUtils.isEmpty(orderItems)){
            for (MallOrderItem orderItem : orderItems) {
                // 如果是秒杀商品，注意：这里已经限制了秒杀商品只能购买一件
                if(orderItem.getSeckillId() != null){
                    Long seckillId = orderItem.getSeckillId();
                    MallSeckill seckill = mallSeckillService.getById(seckillId);
                    if(seckill != null)
                        seckill.setSeckillNum(seckill.getSeckillNum() + 1);
                    boolean updatedSeckill = mallSeckillService.updateById(seckill);
                    if(!updatedSeckill){
                        throw new RuntimeException("秒杀商品货品库存增加失败");
                    }
                    mallRedisCache.increment(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + seckillId);
                }else {// 普通商品
                    Long goodsId = orderItem.getGoodsId();
                    Integer goodsCount = orderItem.getGoodsCount();
                    MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
                    if(goodsInfo != null)
                        goodsInfo.setStockNum(goodsInfo.getStockNum() + goodsCount);
                    boolean updatedGoods = mallGoodsInfoService.updateById(goodsInfo);
                    if(!updatedGoods){
                        throw new RuntimeException("商品货品库存增加失败");
                    }
                }
            }
        }

        // 返还优惠券
        boolean updatedCoupon = mallCouponService.releaseCoupon(orderId);
        if(!updatedCoupon){
            throw new RuntimeException("优惠券返还失败");
        }

        log.info("系统结束处理延时任务---订单超时未付款--- {}", this.orderId);
    }
}
