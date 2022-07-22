package com.ljf.rabbitmq;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@Configuration
@EnableRabbit
public class MQConfig {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1.spring.rabbitmq.publisher-confirms=true
     * 2.设置确认回调ConfirmCallback
     * 只要我们的MQ代理/Broker收到，这个方法就会自动调用
     *
     * 消息正确抵达队列进行回调
     * 1.spring.rabbitmq.publisher-returns=true
     *   spring.rabbitmq.template.mandatory=true
     * 2.设置确认回调ReturnsCallback
     *
     * 消费端确认（保证每一个消息都被正确消费，此时才可以broker删除这个消息）：
     *  1.默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *      问题：
     *          收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了，发生消息丢失；
     *          即：默认是Auto，就是消息一抵达consumer，就自动回复，并不在意消息是否成功处理
     *      解决：手动确认，处理一个确认一个；只要我们没有明确告诉MQ，货物被签收，没有ack，消息就一直是unacked
     *          状态，即使Consumer宕机，消息不会丢失，会重新变为ready状态，下一次有新的consumer连接进来就发给他
     *          spring.rabbitmq.listener.simple.acknowledge-mode=manual   手动签收
     *  2.如何接收？
     *      channel.basicAck(deliveryTag,false)：签收，业务成功完成就应该签收
     *      channel.basicNack(deliveryTag,false,true)：拒签，业务失败拒签
     * */
    @PostConstruct // 配置类构造器创建完成之后执行该方法
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * CorrelationData correlationData:关联消息的唯一关联数据(这个是消息的唯一ID)
             * boolean ack:消息是否成功收到
             * String cause:失败的原因
             * */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm...");
                System.out.println(correlationData);
                System.out.println(ack);
                System.out.println(cause);
            }
        });

        /**
         * 设置消息抵达队列的确认回调
         *
         * 触发时机：只有you消息没有投递给指定的队列，就触发这个失败回调
         * */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            /**
             * ReturnedMessage:
             *  Message message;// 投递失败的消息详细信息
             * 	int replyCode;// 回复的状态码
             * 	String replyText;// 回复的文本内容
             * 	String exchange;// 当时这个消息发给那个交换机
             * 	String routingKey;// 当时这个消息发给那个路由键
             * */
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println(returned);
            }
        });

    }

}
