package com.ljf.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 *  Spring框架中的spring-data-redis模块对Jedis API的进行了高度封装，
 * 提供了在Spring应用中通过简单的连接池信息配置就可以访问Redis服务并进行相关缓存操作。
 * SpringDataRedis相对于Jedis来说可以方便地更换Redis的Java客户端
 * 如多线程安全的Lettuce，比Jedis多了自动管理连接池的特性，
 * 不需要我们自己的JediPoolUtil封装工具类。
 *  它还默认提供了两个使用Redis的类StringRedisTemplate和RedisTemplate，
 * 其中RedisTemplate可以支持Redis没有的缓存对象的操作，
 * 而StringRedisTemplate用来存储字符串。
 * (其实它们都是RedisTemplate<K, V>泛型接口的实现类,我们可以自定义模板然后@AutoWired注入IOC容器中使用)
 *
 * ⭐ SpringBoot 2.0已经使用Lettuce代替Jedis
 *  🔮 其实，随着Spring Boot2.x的到来，支持的组件越来越丰富，也越来越成熟，
 * 其中对Redis的支持不仅仅是丰富了它的API，更是替换掉底层Jedis的依赖，
 * 取而代之换成了Lettuce高级Redis客户端，用于多线程安全同步，异步和响应使用。
 *  🔮 Lettuce和Jedis的都是连接Redis Server的客户端程序。Jedis在实现上是直连redis server，
 * 多线程环境下非线程安全，除非使用连接池JedisPool，为每个Jedis实例增加物理连接。
 * Lettuce基于Netty的连接实例（StatefulRedisConnection），可以在多个线程间并发访问，
 * 且线程安全，满足多线程环境下的并发访问，同时它是可伸缩的设计，
 * 一个连接实例不够的情况也可以按需增加连接实例。
 *  🔮 为了多线程安全，以前是Jedis+JedisPool组合 ,
 * 现在在SpringBoot2.0应用中直接使用Lettuce客户端的API封装RedisTemplate即可，
 * 只要配置好连接池属性，那么SpringBoot就能自动管理连接池。
 *
 *  当我们的数据存储到 Redis 的时候，我们的键（key）和值（value）都是通过 Spring 提供的
 * Serializer 序列化到数据库的。
 *  RedisTemplate 默认使用的是 JdkSerializationRedisSerializer，
 * StringRedisTemplate 默认使用的是 StringRedisSerializer。
 * */
@Configuration
@EnableCaching // 开启缓存
public class RedisCacheConfig extends CachingConfigurerSupport {
    // @Value("spring.redis.expire")
    private int expire = 400;

    //@Autowired
    //private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory){
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(factory);
        return stringRedisTemplate;
    }

    /*@Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setConnectionFactory(factory);
        //key序列化方式
        template.setKeySerializer(redisSerializer);
        //value序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //value hashmap序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        return template;
    }*/

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);
        // key序列化方式
        redisTemplate.setKeySerializer(keySerializer());
        // value序列化方式
        redisTemplate.setValueSerializer(valueSerializer());
        // value HashMap序列化方式
        redisTemplate.setHashValueSerializer(valueSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }
    private RedisSerializer<Object> valueSerializer() {
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
        ParserConfig globalInstance = ParserConfig.getGlobalInstance();

        globalInstance.setAutoTypeSupport(true);
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setParserConfig(globalInstance);
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);
        fastJsonRedisSerializer.setFastJsonConfig(fastJsonConfig);
        return fastJsonRedisSerializer;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfig())
                .transactionAware()
                .build();
    }
    private RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("redis_key")
                .entryTtl(Duration.ofSeconds(expire))
                .disableCachingNullValues();
    }
    @Bean(name = "cacheKeyGenerator")
    public KeyGenerator cacheKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    /*@Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        //解决查询缓存转换异常的问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // 配置序列化（解决乱码的问题）,过期时间600秒
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues();
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
        return cacheManager;
    }*/


}
