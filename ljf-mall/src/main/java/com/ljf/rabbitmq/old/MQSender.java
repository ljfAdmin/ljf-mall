//package com.ljf.rabbitmq;
//
//import com.alibaba.fastjson.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.AmqpTemplate;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class MQSender {
//    private static Logger log = LoggerFactory.getLogger(MQSender.class);
//
//    @Autowired
//    private AmqpTemplate amqpTemplate;
//
//    public void sendSeckillMessage(SeckillMessage seckillMessage) {
//        String msg = JSONObject.toJSONString(seckillMessage);
//        log.info("send message:" + msg);
//        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);
//    }
//
//
//    // @Autowired
//    // private RabbitTemplate rabbitTemplate;
//    /**
//     * 站内信
//     */
//    /*public void sendMessage(SeckillMessage mm) {
//        log.info("send message:" + "11111");
//        rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC, "seckill_*", "111111111");
//    }*/
//
//    /**
//     * 站内信
//     */
//    /*public void sendRegisterMessage(SeckillMessage seckillMessage) {
//        String msg = JSONObject.toJSONString(seckillMessage);
//        log.info("send message:{}", msg);
//        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_TEST, msg);
//        // rabbitTemplate.convertAndSend(MQConfig.EXCHANGE_TOPIC,"miaosha_*", msg);
//    }*/
//}
