package com.ljf;

import com.ljf.entity.vo.MallUserVO;
import com.rabbitmq.client.Channel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.UUID;

/**
 * 使用RabbitMQ:
 * 1.引入AMQP场景：RabbitAutoConfiguration就会自动生效
 * 2.给容器中自动配置了RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate等
 *     所有的属性都是@EnableConfigurationProperties(RabbitProperties.class) - @ConfigurationProperties(prefix = "spring.rabbitmq")配置
 * 3.给配置文件配置spring.rabbitmq信息
 * 4.@EnableRabbit注解开启功能
 * 5.监听消息：使用@RabbitListener，必须有4步骤
 *      @RabbitListener: 类+方法上
 *      @RabbitHandler: 标在方法上（重载监听不同的消息）
 *
 *      可以在类上添加@RabbitListener注解，而在对应的的方法上标注@RabbitHandler
 *      为何不直接在方法上标注@RabbitListener？
 *          针对接收消息中的对象类型可以设置重载
 * */

// 加载 Spring 配置文件的注解
// @ContextConfiguration(locations = {"classpath:spring-persist-mybatis.xml"})
// 指定 Spring 给 Junit 提供的运行器类
// SpringRunner extends SpringJUnit4ClassRunner
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitTest {
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 1.如何创建交换机、队列以及之间的绑定关系
     *   ①使用AmqpAdmin进行创建
     *   ②
     * 2.如何收发关系
     *
     * */
    @Test
    public void createExchange(){
        /**
         * DirectExchange(String name)
         * public DirectExchange(String name, boolean durable, boolean autoDelete)
         * public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         * */
        DirectExchange directExchange = new DirectExchange("hello-exchange",true,false,null);
        amqpAdmin.declareExchange(directExchange);
        System.out.println("exchange创建成功，名字是："+directExchange.getName());
    }

    /**
     * 创建队列
     * */
    @Test
    public void createQueue(){
        /**
         * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
         * 排他的：队列只能被声明连接使用，队列都不应该是排他的
         * */
        Queue queue = new Queue("hello-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        System.out.println("队列创建成功" + queue.getName() + " " + queue.getActualName());
    }

    /**
     * 创建绑定
     * Binding(String destination, Binding.DestinationType destinationType,
     *          String exchange, String routingKey,
     *          @Nullable Map<String, Object> arguments)
     *  目的地，目的地类型，交换机，路由键，自定义参数
     * */
    @Test
    public void createBinding(){
        Binding binding = new Binding("hello-queue", Binding.DestinationType.QUEUE,
                "hello-exchange","hello",null);
        amqpAdmin.declareBinding(binding);

        System.out.println("绑定创建成功"+binding);//[destination=hello-queue, exchange=hello-exchange, routingKey=hello, arguments={}]
    }

    /**
     * 发送消息
     * */
    @Test
    public void sendMessage(){
        // 如果发送消息，发送的消息是个对象，会使用序列化方式机制，将对象写出去，对象必须实现Serializable接口
        // 发送的对象类型的消息可以是一个JSON
        //rabbitTemplate.convertAndSend("hello-exchange","hello","hello world");

        MallUserVO mallUserVO = new MallUserVO();
        mallUserVO.setAddress("ljfa");
        mallUserVO.setLoginName("ljfa");
        mallUserVO.setNickName("ljfa");
        mallUserVO.setIntroduceSign("ljfa");
        mallUserVO.setShopCartItemCount(1);
        //priority:	0
        //delivery_mode: 2
        //headers:__TypeId__:	com.ljf.entity.vo.MallUserVO
        //content_encoding:	UTF-8
        //content_type:	application/json
        //{"userId":null,"nickName":"ljfa","loginName":"ljfa","introduceSign":"ljfa","address":"ljfa","shopCartItemCount":1}
        //rabbitTemplate.convertAndSend("hello-exchange","hello",mallUserVO);
        rabbitTemplate.convertAndSend("hello-exchange","hello",mallUserVO,
                new CorrelationData(UUID.randomUUID().toString()));
        System.out.println("消息发送成功"+mallUserVO);
    }

    /**
     * 接收消息：参数可以写一下类型
     *  1.Message message：原生消息详细信息，头+体
     *  2.Object/T<发送的消息类型>：MallUserVO mallUserVO
     *  3.Channel channel：当前传输数据的通道
     *
     * Queue：可以很多人都来监听，只要收到消息队列删除消息，而且只能有一个收到消息
     *  场景：
     *      1.订单服务启动多个：同一个消息只能有一个客户端收到
     *      2.模拟业务处理很耗时，能不能接收其他消息，只有当一个消息完全处理完，方法运行结束，可以接收下一个消息
     *
     *
     * */
    @RabbitListener(queues = {"hello-queue"})
    public void receiveMessage(Message message, MallUserVO mallUserVO, Channel channel){
        // (Body:'[B@6d1d372c(byte[114])'
        //  MessageProperties [headers={__TypeId__=com.ljf.entity.vo.MallUserVO},
        //                     contentType=application/json,
        //                     contentEncoding=UTF-8,
        //                     contentLength=0,
        //                     receivedDeliveryMode=PERSISTENT,
        //                     priority=0,
        //                     redelivered=false,
        //                     receivedExchange=hello-exchange,
        //                     receivedRoutingKey=hello,
        //                     deliveryTag=2,
        //                     consumerTag=amq.ctag-1cIykaVvGvg1QxyEkZ-vJA,
        //                     consumerQueue=hello-queue])
        byte[] body = message.getBody();
        MessageProperties messageProperties = message.getMessageProperties();

        System.out.println(message.getClass());//class org.springframework.amqp.core.Message
        System.out.println(mallUserVO);//MallUserVO(userId=null, nickName=ljfa, loginName=ljfa, introduceSign=ljfa, address=ljfa, shopCartItemCount=1)

        // 自增的，通道内自增的，按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(deliveryTag);

        // 签收货物
        // 是否批量签收货物false
        try {
            if(deliveryTag % 2 == 0){
                // 收货
                channel.basicAck(deliveryTag,false);
                System.out.println("签收了货物");
            }else{
                // 退货
                // void basicNack(long var1, boolean var3, boolean var4)
                // 如果拒绝的消息应重新排队而不是丢弃/死信，则重新排队为true
                // true：发挥服务器，重新入队；  false：丢弃
                channel.basicNack(deliveryTag,false,false);
                // void basicReject(long var1, boolean var3)
                //channel.basicReject(deliveryTag,false);
                System.out.println("没有签收了货物");
            }
        } catch (IOException e) {
            // 网络中断
            e.printStackTrace();
        }
    }

}
