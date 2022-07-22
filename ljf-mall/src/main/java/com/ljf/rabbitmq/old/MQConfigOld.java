//package com.ljf.rabbitmq;
//
//import org.springframework.amqp.core.*;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class MQConfigOld {
//    /**
//     * /usr/sbin/rabbitmq-plugins enable rabbitmq_management
//     * mq页面
//     */
//    public static final String EXCHANGE_TOPIC = "exchange_topic";
//    public static final String MIAOSHA_MESSAGE = "miaosha_mess";
//    public static final String MIAOSHA_TEST = "miaosha_test";
//
//    public static final String MIAOSHA_QUEUE = "miaosha.queue";
//    public static final String DIRECT_QUEUE = "direct.queue";
//    public static final String FANOUT_QUEUE = "fanout.queue";
//    public static final String TOPIC_QUEUE1 = "topic.queue1";
//    public static final String TOPIC_QUEUE2 = "topic.queue2";
//    public static final String HEADER_QUEUE = "header.queue";
//
//    /**
//     * headers 匹配 AMQP 消息的 header 而不是路由键，此外 headers 交换器和 direct 交换器完全一致，
//     * 但性能差很多，目前几乎用不到了
//     * */
//    public static final String TOPIC_EXCHANGE = "topicExchange";
//    public static final String FANOUT_EXCHANGE = "fanoutExchange";
//    public static final String DIRECT_EXCHANGE = "directExchange";
//    public static final String HEADERS_EXCHANGE = "headersExchange";
//
//
//
//    /**
//     * Direct模式  交换机Exchange
//     */
//    @Bean
//    public Queue directQueue() {
//        return new Queue(DIRECT_QUEUE,true);
//    }
//    /**
//     * 创建一个指定名称为 DIRECT_EXCHANGE = "directExchange" 的Direct类型的Exchange交换机
//     */
//    @Bean
//    public Exchange directExchange() {
//        return new DirectExchange(DIRECT_EXCHANGE);
//    }
//
//
//
//    /**
//     * Fanout模式 交换机Exchange
//     */
//    @Bean
//    public Queue fanoutQueue() {
//        return new Queue(FANOUT_QUEUE,true);
//    }
//    /**
//     * 创建一个指定名称为 FANOUT_EXCHANGE = "fanoutExchange" 的Fanout类型的Exchange交换机
//     */
//    @Bean
//    public FanoutExchange fanoutExchange() {
//        return new FanoutExchange(FANOUT_EXCHANGE);
//    }
//
//    /**
//     * 将队列绑定到交换机
//     */
//    //参数 1:为自定义队列对象，参数名queue为自定义队列Bean的id
//    //参数 2:为自定义的交换机，参数名myChange为自定义交换机Bean的id
//    @Bean("fanoutBinding1")
//    public Binding FanoutBinding1() {
//        // 将队列绑定到交换机，参数BootRouting为RoutingKey
//        // return BindingBuilder.bind(queue).to(myChange).with("BootRouting ").noargs();
//        return BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
//    }
//    @Bean("fanoutBinding2")
//    public Binding FanoutBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
//    }
//
//
//
//    /**
//     * Topic模式 交换机Exchange
//     */
//    @Bean
//    public Queue topicQueue1() {
//        return new Queue(TOPIC_QUEUE1, true);
//    }
//    @Bean
//    public Queue topicQueue2() {
//        return new Queue(TOPIC_QUEUE2, true);
//    }
//    /**
//     * 创建一个指定名称为 TOPIC_EXCHANGE = "topicExchange" 的Topic类型的Exchange交换机
//     */
//    @Bean
//    public TopicExchange topicExchange() {
//        return new TopicExchange(TOPIC_EXCHANGE);
//    }
//    /**
//     * 将队列绑定到交换机
//     *  会识别两个通配符：符号“#”和符号“*”。#匹配0个或多个单词，“*”匹配不多不少一个单词
//     */
//    //参数 1:为自定义队列对象，参数名queue为自定义队列Bean的id
//    //参数 2:为自定义的交换机，参数名myChange为自定义交换机Bean的id
//    @Bean("topicBinding1")
//    public Binding topicBinding1() {
//        // 将队列绑定到交换机，参数BootRouting为RoutingKey
//        // return BindingBuilder.bind(queue).to(myChange).with("BootRouting ").noargs();
//        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
//    }
//    @Bean("topicBinding2")
//    public Binding topicBinding2() {
//        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("topic.#");
//    }
//
//
//
//    /**
//     * Header模式 交换机Exchange   基本不用，可以使用Direct类型的交换机取代
//     */
//    @Bean
//    public Queue headerQueue() {
//        return new Queue(HEADER_QUEUE, true);
//    }
//    /**
//     * 创建一个指定名称为 HEADERS_EXCHANGE = "headersExchange" 的Headers类型的Exchange交换机
//     */
//    @Bean
//    public HeadersExchange headersExchange() {
//        return new HeadersExchange(HEADERS_EXCHANGE);
//    }
//
//    @Bean
//    public Binding headerBinding() {
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("header1", "value1");
//        map.put("header2", "value2");
//        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
//    }
//
//}
