package com.ljf.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class GlobalEntityMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // MallCoupon、MallCarousel、MallUser、MallUserCouponRecord、MallShoppingCartItem、MallSeckill、MallOrder、MallIndexConfig、MallGoodsCategory
        this.setFieldValByName("isDeleted", 0, metaObject);
        this.setFieldValByName("lockedFlag", 0, metaObject);//MallUser
        // MallCoupon、MallCarousel、MallUser、MallUserCouponRecord、MallShoppingCartItem、MallSeckillSuccess、MallSeckill、MallOrderItem、MallOrder、MallIndexConfig、MallGoodsInfo、MallGoodsCategory
        this.setFieldValByName("createTime", new Date(), metaObject);
        // MallCoupon、MallCarousel、MallUserCouponRecord、MallShoppingCartItem、MallSeckill、MallOrder、MallIndexConfig、MallGoodsInfo、MallGoodsCategory
        this.setFieldValByName("updateTime", new Date(), metaObject);

        this.setFieldValByName("useStatus",0,metaObject);//MallUserCouponRecord

        this.setFieldValByName("locked",0,metaObject);//MallAdminUser
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // MallCoupon、MallCarousel、MallUserCouponRecord、MallShoppingCartItem、MallSeckill、MallOrder、MallIndexConfig、MallGoodsInfo、MallGoodsCategory
        this.setFieldValByName("updateTime", new Date(), metaObject);//MallUserCouponRecord
    }
}
