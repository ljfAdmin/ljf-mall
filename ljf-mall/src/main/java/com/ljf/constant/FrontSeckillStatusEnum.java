package com.ljf.constant;

/**
 * 秒杀商品状态enum
 * */
public enum FrontSeckillStatusEnum {
    // 未到秒杀开始时间
    NOT_START,
    // 到了秒杀开始时间
    START,
    // 到了秒杀开始时间，但是秒杀商品库存不足
    STARTED_SHORTAGE_STOCK;
}
