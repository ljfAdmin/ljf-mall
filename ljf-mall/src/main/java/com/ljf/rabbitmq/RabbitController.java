package com.ljf.rabbitmq;

import com.ljf.entity.MallUser;
import com.ljf.entity.vo.MallCouponVO;
import com.ljf.entity.vo.MallUserVO;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class RabbitController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping(value = "/send/message")
    public String sendMessage(@RequestParam(defaultValue = "10") Integer num){
        for (int i = 0; i < num; i++) {
            //针对不同的处理发送不能类型对象的消息
            if(i % 2 == 0){
                MallUserVO mallUserVO = new MallUserVO();
                mallUserVO.setShopCartItemCount(i);
                // rabbitTemplate.convertAndSend("hello-exchange","hello",mallUserVO);
                rabbitTemplate.convertAndSend("hello-exchange","hello",
                        mallUserVO,new CorrelationData(UUID.randomUUID().toString()));
            }else{
                MallCouponVO mallCouponVO = new MallCouponVO();
                mallCouponVO.setCouponLimit(i);
                // rabbitTemplate.convertAndSend("hello-exchange","hello",mallCouponVO);
                rabbitTemplate.convertAndSend("hello-exchange","hello",
                        mallCouponVO,new CorrelationData(UUID.randomUUID().toString()));
            }
        }

        return "ok";
    }
}
