package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.util.concurrent.RateLimiter;
import com.ljf.constant.enums.FrontSeckillStatusEnum;
import com.ljf.constant.MallSeckillInfoConstant;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallSeckill;
import com.ljf.entity.MallSeckillSuccess;
import com.ljf.entity.vo.MallSeckillGoodsVO;
import com.ljf.entity.vo.MallSeckillSuccessVO;
import com.ljf.entity.vo.SeckillInterfaceExposeVO;
import com.ljf.mapper.MallSeckillMapper;
import com.ljf.redis.MallRedisCache;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.service.MallSeckillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.service.MallSeckillSuccessService;
import com.ljf.utils.MD5Util;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
/**
 * 在开发高并发系统时有三剑客：缓存、降级和限流。
 *     缓存 缓存的目的是提升系统访问速度和增大系统处理容量。
 *     降级 降级是当服务出现问题或者影响到核心流程时，需要暂时屏蔽掉，待高峰或者问题解决后再打开。
 *     限流 限流的目的是通过对并发访问/请求进行限速，或者对一个时间窗口内的请求进行限速来保护系统，
 *          一旦达到限制速率则可以拒绝服务、排队或等待、降级等处理。
 * */
@Service
public class MallSeckillServiceImpl extends ServiceImpl<MallSeckillMapper, MallSeckill> implements MallSeckillService {
    // 服务限流：服务限流是限制请求的数量，即某个时间窗口内的请求速率。一旦达到限制速率则可以拒绝服务
    //  （定向到错误页或告知系统忙），排队等待（比如秒杀，用户评论，下单），降级（返回兜底数据或默认数据）。

    // 常见的限流方法：
    //  常见的限流手段有如下这些。限制总的并发数（比如数据库连接池，线程池），
    //  限制瞬时并发数（如nginx的limit_conn模块，用来限制瞬时并发连接数），
    //  限制某个时间窗口内的平均速率（RateLimiter，nginx的limit_req模块）；
    //  此外还有限制RPC调用频率，限制MQ的消费速率等。

    // 限流工具类 / 使用令牌桶RateLimiter 限流
    // create():每秒创建多少令牌
    // 　guava的RateLimiter使用的是令牌桶算法，也就是以固定的频率向桶中放入令牌，例如一秒钟10枚令牌，
    // 实际业务在每次响应请求之前都从桶中获取令牌，只有取到令牌的请求才会被成功响应，
    // 获取的方式有两种：阻塞等待令牌或者取不到立即返回失败
    private static final RateLimiter rateLimiter = RateLimiter.create(100);// 每秒创建100个令牌

    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Autowired
    private MallRedisCache mallRedisCache;

    @Autowired
    private MallSeckillSuccessService mallSeckillSuccessService;

    @Override
    @Transactional // 这里开启事务
    public boolean saveSeckill(MallSeckill mallSeckill) {
        // 首先先判断该秒杀单子的商品ID是否有对应的商品信息
        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(mallSeckill.getGoodsId());
        if(goodsInfo == null){
            return false;
        }
        return baseMapper.insert(mallSeckill) > 0;
    }

    @Override
    @Transactional // 这里开启事务
    public boolean updateSeckill(MallSeckill mallSeckill) {
        // 首先仍然是判断该秒杀单子的商品ID是否有对应的商品信息
        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(mallSeckill.getGoodsId());
        if(goodsInfo == null){
            return false;
        }
        // 看是否存在该秒杀信息
        MallSeckill seckillTemp = baseMapper.selectById(mallSeckill.getSeckillId());
        if(seckillTemp == null){
            return false;
        }

        return baseMapper.updateById(mallSeckill) > 0;
    }

    @Override
    public SeckillInterfaceExposeVO exposerUrl(Long seckillId) {
        // seckill_goods_detail:
        MallSeckillGoodsVO mallSeckillGoodsVO = mallRedisCache.getCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_DETAIL + seckillId);
        Date seckillBegin = mallSeckillGoodsVO.getSeckillBegin();
        Date seckillEnd = mallSeckillGoodsVO.getSeckillEnd();

        // 系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < seckillBegin.getTime() || nowTime.getTime() > seckillEnd.getTime()) {
            return new SeckillInterfaceExposeVO(FrontSeckillStatusEnum.NOT_START, seckillId, nowTime.getTime(), seckillBegin.getTime(), seckillEnd.getTime());
        }
        // 检查虚拟库存
        // seckill_goods_stock:
        Integer stock = mallRedisCache.getCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return new SeckillInterfaceExposeVO(FrontSeckillStatusEnum.STARTED_SHORTAGE_STOCK, seckillId);
        }
        // 加密
        String md5 = MD5Util.encrypt(seckillId.toString());
        return new SeckillInterfaceExposeVO(FrontSeckillStatusEnum.START, seckillId, md5);
    }

    /**
     * 执行秒杀
     * */
    @Override
    public MallSeckillSuccessVO executeSeckill(Long seckillId, Long userId) throws Exception {
        /**
         * public boolean tryAcquire(long timeout, TimeUnit unit) {
         *     return tryAcquire(1, timeout, unit);
         * }
         *
         * 尝试获取令牌，tryAcquire():尝试获取一个令牌，如果获取不到立即返回；
         * tryAcquire(int permits, long timeout, TimeUnit unit):尝试获取permits个令牌，
         *              如果获取不到等待timeout时间
         * */
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!rateLimiter.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            throw new Exception("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (mallRedisCache.containsCacheSet(MallSeckillInfoConstant.SECKILL_SUCCESS_USER_ID + seckillId, userId)) {
            throw new Exception("您已经购买过秒杀商品，请勿重复购买");
        }

        // 更新秒杀商品虚拟库存
        Long stock = mallRedisCache.luaDecrement(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock < 0) {
            throw new Exception("秒杀商品已售空");
        }

        // seckill:
        MallSeckill mallSeckill = mallRedisCache.getCacheObject(MallSeckillInfoConstant.SECKILL_KEY + seckillId);
        if (mallSeckill == null) {
            mallSeckill = baseMapper.selectById(seckillId);
            mallRedisCache.setCacheObject(MallSeckillInfoConstant.SECKILL_KEY + seckillId, mallSeckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = mallSeckill.getSeckillBegin().getTime();
        long endTime = mallSeckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new Exception("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new Exception("秒杀已结束");
        }

        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>(8);
        map.put("seckillId", seckillId);
        map.put("userId", userId);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行存储过程，result被赋值
        try {
            baseMapper.killByProcedure(map);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        // 获取result -2sql执行失败 -1未插入数据 0未更新数据 1sql执行成功
        map.get("result");
        int result = MapUtils.getInteger(map, "result", -2);
        if (result != 1) {
            throw new Exception("很遗憾！未抢购到秒杀商品");
        }

        // 记录购买过的用户   seckill_success_user_id:
        mallRedisCache.setCacheSet(MallSeckillInfoConstant.SECKILL_SUCCESS_USER_ID + seckillId, userId);
        long endExpireTime = endTime / 1000;
        long nowExpireTime = nowTime / 1000;
        mallRedisCache.expire(MallSeckillInfoConstant.SECKILL_SUCCESS_USER_ID + seckillId, endExpireTime - nowExpireTime, TimeUnit.SECONDS);

        QueryWrapper<MallSeckillSuccess> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("seckill_id",seckillId);
        MallSeckillSuccess seckillSuccess = mallSeckillSuccessService.getOne(queryWrapper);

        MallSeckillSuccessVO seckillSuccessVO = new MallSeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSecId(seckillSuccessId);
        seckillSuccessVO.setMd5(MD5Util.encrypt(seckillSuccessId + MallSeckillInfoConstant.SECKILL_ORDER_SALT));

        return seckillSuccessVO;
    }
}
