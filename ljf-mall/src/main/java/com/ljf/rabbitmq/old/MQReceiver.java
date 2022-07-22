//package com.ljf.rabbitmq;
//
//import com.alibaba.fastjson.JSONObject;
//import com.ljf.entity.MallGoodsInfo;
//import com.ljf.entity.vo.MallUserVO;
//import com.ljf.redis.RedisCache;
//import com.ljf.service.MallGoodsInfoService;
//import com.ljf.service.MallOrderService;
//import com.ljf.service.MallSeckillService;
//import com.ljf.service.MallSeckillSuccessService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class MQReceiver {
//    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
//
//    @Autowired
//    private RedisCache redisCache;
//
//    @Autowired
//    private MallGoodsInfoService mallGoodsInfoService;
//
//    @Autowired
//    private MallOrderService mallOrderService;
//
//    @Autowired
//    private MallSeckillService mallSeckillService;
//
//	@Autowired
//    private MallSeckillSuccessService mallSeckillSuccessService ;
//
//    //  @RabbitListener注解用于标记当前方法为消息监听方法，可以监听某个队列，
//    // 当队列中有新消息则自动完成接收
//    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
//    public void receive(String message) {
//        log.info("receive message:" + message);
//        SeckillMessage seckillMessage = (SeckillMessage) JSONObject.toJSON(message);
//        MallUserVO mallUserVO = seckillMessage.getMallUserVO();
//        Long goodsId = seckillMessage.getGoodsId();
//
//        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
//
//        /*ResultGeekQOrder<GoodsVoOrder> goodsVoOrderResultGeekQOrder = goodsServiceRpc.getGoodsVoByGoodsId(goodsId);
//        if (!AbstractResultOrder.isSuccess(goodsVoOrderResultGeekQOrder)) {
//            throw new GlobleException(ResultStatus.SESSION_ERROR);
//        }
//        GoodsOrderVO goods = goodsVoOrderResultGeekQOrder.getData();*/
//
//        Integer stockNum = goodsInfo.getStockNum();
//        if(stockNum == null)
//            return;
//
//        int stockCountInt = stockNum.intValue();
//        if (stockCountInt <= 0) {
//            return;
//        }
//
//        //判断是否已经秒杀到了
//        MiaoshaOrder miaoshaOrder = miaoshaOrderService.getMiaoshaOrderByUserIdAndGoodsId(Long.valueOf(user.getId()), goodsId);
//        if (miaoshaOrder == null) {
//            return;
//        }
//        //减库存 下订单 写入秒杀订单
//        miaoshaService.miaosha(user, goods);
//    }
//}
