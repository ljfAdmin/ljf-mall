package com.ljf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.enums.MallGoodsCategoryLevelEnum;
import com.ljf.entity.MallGoodsCategory;
import com.ljf.entity.vo.MallIndexGoodsCategoryVO;
import com.ljf.entity.vo.MallIndexSecondLevelCategoryVO;
import com.ljf.entity.vo.MallIndexThirdLevelCategoryVO;
import com.ljf.entity.vo.SearchPageCategoryVO;
import com.ljf.mapper.MallGoodsCategoryMapper;
import com.ljf.redis.MallRedisCache;
import com.ljf.service.MallGoodsCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.utils.BeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallGoodsCategoryServiceImpl extends ServiceImpl<MallGoodsCategoryMapper, MallGoodsCategory> implements MallGoodsCategoryService {
    @Autowired
    private MallRedisCache mallRedisCache;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 下面方法的改进：只用查询一次数据库即可
     * */
    @Override
    public List<MallGoodsCategory> getAppointedLevelGoodsCategoriesFromAll(long parentId, Integer categoryLevel, List<MallGoodsCategory> allGoodsCategories) {
        List<MallGoodsCategory> ans = new ArrayList<>();
        for (MallGoodsCategory allGoodsCategory : allGoodsCategories) {
            if(allGoodsCategory.getParentId() == parentId && categoryLevel.equals(allGoodsCategory.getCategoryLevel())){
                ans.add(allGoodsCategory);
            }
        }
        return ans;
    }
    /**
     * 抽取方法，返回指定某个指定层级的分类下的所有子分类
     * */
    @Override
    public List<MallGoodsCategory> getAppointedLevelGoodsCategories(Long parentId, Integer categoryLevel) {
        QueryWrapper<MallGoodsCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",parentId);
        queryWrapper.eq("category_level",categoryLevel);
        queryWrapper.orderByDesc("category_rank");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * public List<MallIndexGoodsCategoryVO> getCategoriesForIndex() 的缓存版本2+SpringCache
     *
     * 缓存：整合SpringCache简化缓存开发，参考本类中的SearchPageCategoryVO getCategoriesForSearch(Long categoryId)
     *  1.引入依赖  SpringCache
     *  2.写配置
     *      ①自动配置：CacheAutoConfiguration.java会导入RedisCacheConfiguration，自动配好了cacheManager
     *      ②手动配置：配置使用redis作为缓存
     *  3.测试使用缓存
     *      @Cacheable:触发将数据保存到缓存的操作
     *      @CacheEvict:触发将数据从缓存删除的操作   失效模式
     *             指定删除某个分区下的所有数据：@CacheEvict(value = {"category"},allEntries = true)
     *                                      或者使用Caching注解挨个声明
     *             因此存储同一类型的数据，都可以指定成同一个分区，比如这里的分类信息都指定分区为category
     *             默认分区明就是缓存的前缀   category:key
     *      @CachePut:不影响方法执行更新缓存   双写模式
     *      @Caching:组合以上多个操作
     *          同时进行多种缓存操作
     *      @CacheConfig:在类级别共享缓存的相关配置
     *  4.开启缓存功能：spring.cache.type=redis，只需要使用注解即可
     *
     *  5.原理：
     *      CacheAutoConfiguration --> RedisCacheConfiguration --> 自动配置了RedisCacheManager
     *      --> 根据配置名字初始化所有的缓存 --> 每个缓存决定使用什么配置，如果redisConfiguration有
     *      就用已有的，如果没有就用默认配置 --> 想改缓存的配置只需要给容器中加上RedisCacheConfiguration即可
     *      --> 就会应用到当前缓存管理器RedisCacheManager管理的所有缓存分区中
     *
     *  6.SpringCache的不足：
     *      1.读模式
     *          缓存穿透：查询一个null数据，解决：缓存空数据  spring.cache.redis.cache-null-values=true
     *          缓存击穿：大量并发进来同时查询一个正好过期的数据，解决：加锁？源码172：默认是无加锁的，
     *                  但是只有读模式下可以设置
     *          缓存雪崩：大量的key同时过期，解决：加随机时间，指定过期时间即可  spring.cache.redis.time-to-live=3600000
     *      2.写模式：（缓存与数据库一致）
     *          读写加锁
     *          引入Canal，感知到Mysql的更新去更新数据库
     *          读多写多，直接去数据库查询即可；
     *
     *  7.原理：CacheManager --> Cache --> Cache负责缓存的读写
     *
     *  总结： 常规数据（读多写少，即时性，一致性要求不高的数据），完全可以使用SpringCache，
     *      写模式（只要缓存的数据有过期时间就足够了）
     *         特殊数据：特殊设计
     * */
    @Cacheable(value = {"category"},key = "#root.methodName",sync = true)
    public List<MallIndexGoodsCategoryVO> getCategoriesForIndex2() {
        /*
        * getCategoriesForIndexDB的逻辑
        * */
        List<MallIndexGoodsCategoryVO> categoryVOList = new ArrayList<>();
        List<MallGoodsCategory> allGoodsCategories = baseMapper.selectList(null);

        // 获取一级分类的固定数量的数据
        List<MallGoodsCategory> firstLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevelEnum.LEVEL_ONE.getLevel(), allGoodsCategories);

        if(!CollectionUtils.isEmpty(firstLevelCategories)){
            // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
            // List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();

            // 根据每个一级分类获取其二级分类数据
            for (MallGoodsCategory firstLevelCategory : firstLevelCategories) {
                // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
                List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();
                List<MallGoodsCategory> secondLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(firstLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_TWO.getLevel(),allGoodsCategories);
                MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();

                if(!CollectionUtils.isEmpty(secondLevelCategories)){
                    // 根据每一个二级分类获取其三级分类数据
                    for (MallGoodsCategory secondLevelCategory : secondLevelCategories) {
                        MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                        List<MallGoodsCategory> thirdLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(secondLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_THREE.getLevel(),allGoodsCategories);
                        //if(!CollectionUtils.isEmpty(thirdLevelCategories)){}

                        List<MallIndexThirdLevelCategoryVO> mallIndexThirdLevelCategoryVOS = BeanUtil.copyList(thirdLevelCategories, MallIndexThirdLevelCategoryVO.class);
                        BeanUtils.copyProperties(secondLevelCategory,mallIndexSecondLevelCategoryVO);
                        mallIndexSecondLevelCategoryVO.setThirdLevelCategoryVOS(mallIndexThirdLevelCategoryVOS);

                        mallIndexSecondLevelCategoryVOS.add(mallIndexSecondLevelCategoryVO);
                    }
                }

                BeanUtils.copyProperties(firstLevelCategory,mallIndexGoodsCategoryVO);
                mallIndexGoodsCategoryVO.setSecondLevelCategoryVOS(mallIndexSecondLevelCategoryVOS);

                categoryVOList.add(mallIndexGoodsCategoryVO);
            }
        }
        return categoryVOList;
    }

    /**
     * public List<MallIndexGoodsCategoryVO> getCategoriesForIndex() 的缓存版本
     *
     * 可能会报异常堆外内存移除：OutOfDirectMemoryError
     *  1.springboot2.0以后默认使用lettuce作为操作redis的客户端，使用netty进行网络通信
     *  2.lettuce的bug导致netty堆外内存溢出，netty如果没有指定堆外内存，默认使用-Xmx指定的大小
     * */
    public List<MallIndexGoodsCategoryVO> getCategoriesForIndex() {
        /*// 1.加入缓存逻辑，缓存中存的数据是JSON字符串，跨语言跨平台
        String categoryAllIndex = (String) redisCache.getCacheObject("categoryAllIndex");
        if(StringUtils.isEmpty(categoryAllIndex) || "null".equals(categoryAllIndex)){
            // 2.缓存中没有，查询数据库
            List<MallIndexGoodsCategoryVO> categoriesForIndexDB = this.getCategoriesForIndexDB();
            // 3.加入缓存，将查出的对象转换为JSON放在缓存中   JSON.toJSONString()
            redisCache.setCacheObject("categoryAllIndex", JSON.toJSONString(categoriesForIndexDB));
            return categoriesForIndexDB;
        }
        // 4.缓存中存在
        List<MallIndexGoodsCategoryVO> categoryVOS = JSON.parseObject(categoryAllIndex, new TypeReference<List<MallIndexGoodsCategoryVO>>() {});
        return categoryVOS;*/

        /**
         * 1.空结果缓存：解决缓存穿透
         * 2.设置过期时间（加随机值）：解决缓存雪崩
         * 3.加锁：解决缓存击穿
         *  加锁位置：当我们第一次查询缓存的时候，如果缓存中没有，假设100万并发，同时判断没有就会都去查询
         *          数据库
         *          方法1：同步代码块synchronized或者同步方法getCategoriesForIndexDB()
         *              ①synchronized(this):springboot所有的组件在容器中都是默认是单例的，这100万个请求都是单例的
         *                  100万个请求同时调用方法，第一个请求数据库之后，并设置到缓存中，但是后续的请求再
         *                  次访问数据库就不合理了，因此再次判断缓存中是否存在数据
         *
         * */
        String categoryAllIndex = stringRedisTemplate.opsForValue().get("categoryAllIndex");
        if(StringUtils.isEmpty(categoryAllIndex)){
            //List<MallIndexGoodsCategoryVO> categoriesForIndexDB = this.getCategoriesForIndexDB();
            List<MallIndexGoodsCategoryVO> categoriesForIndexDB = this.getCategoriesForIndexDBWithSynchronized();
            // 下面的逻辑已经放到了底层去实现
            //stringRedisTemplate.opsForValue().set("categoryAllIndex",JSON.toJSONString(categoriesForIndexDB));
            return categoriesForIndexDB;
        }

        List<MallIndexGoodsCategoryVO> categoryVOS = JSONObject.parseObject(categoryAllIndex, new TypeReference<List<MallIndexGoodsCategoryVO>>() {});
        return categoryVOS;
    }

    /**
     * 本地锁加锁情况下：这种情况在单体应用的情况下，比如本项目，直部署在一台Tomcat服务器上，但是如果分布式则不合适
     *      分布式锁(性能慢)，针对本项目没必要
     * */
    public List<MallIndexGoodsCategoryVO> getCategoriesForIndexDBWithSynchronized() {
        synchronized (this) {
            // 判断缓存中是否存在
            String categoryAllIndex = stringRedisTemplate.opsForValue().get("categoryAllIndex");
            if(!StringUtils.isEmpty(categoryAllIndex)){
                List<MallIndexGoodsCategoryVO> categoryVOS = JSONObject.parseObject(categoryAllIndex, new TypeReference<List<MallIndexGoodsCategoryVO>>() {});
                return categoryVOS;
            }

            // System.out.println("查询了数据库........");

            List<MallIndexGoodsCategoryVO> categoryVOList = new ArrayList<>();
            List<MallGoodsCategory> allGoodsCategories = baseMapper.selectList(null);

            // 获取一级分类的固定数量的数据
            List<MallGoodsCategory> firstLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevelEnum.LEVEL_ONE.getLevel(), allGoodsCategories);

            if(!CollectionUtils.isEmpty(firstLevelCategories)){
                // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
                // List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();

                // 根据每个一级分类获取其二级分类数据
                for (MallGoodsCategory firstLevelCategory : firstLevelCategories) {
                    // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
                    List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();
                    List<MallGoodsCategory> secondLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(firstLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_TWO.getLevel(),allGoodsCategories);
                    MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();

                    if(!CollectionUtils.isEmpty(secondLevelCategories)){
                        // MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                        // 根据每一个二级分类获取其三级分类数据
                        for (MallGoodsCategory secondLevelCategory : secondLevelCategories) {
                            MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                            List<MallGoodsCategory> thirdLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(secondLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_THREE.getLevel(),allGoodsCategories);
                            //if(!CollectionUtils.isEmpty(thirdLevelCategories)){}

                            List<MallIndexThirdLevelCategoryVO> mallIndexThirdLevelCategoryVOS = BeanUtil.copyList(thirdLevelCategories, MallIndexThirdLevelCategoryVO.class);
                            BeanUtils.copyProperties(secondLevelCategory,mallIndexSecondLevelCategoryVO);
                            mallIndexSecondLevelCategoryVO.setThirdLevelCategoryVOS(mallIndexThirdLevelCategoryVOS);

                            mallIndexSecondLevelCategoryVOS.add(mallIndexSecondLevelCategoryVO);
                        }
                    }

                    // MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();
                    BeanUtils.copyProperties(firstLevelCategory,mallIndexGoodsCategoryVO);
                    mallIndexGoodsCategoryVO.setSecondLevelCategoryVOS(mallIndexSecondLevelCategoryVOS);

                    categoryVOList.add(mallIndexGoodsCategoryVO);
                }
            }

            // 结果放入缓存
            stringRedisTemplate.opsForValue().set("categoryAllIndex",JSON.toJSONString(categoryVOList));
            return categoryVOList;
        }

    }

    /**
     * 获取商品分类部分信息显示给前台页面：优化为只需要查询一次数据库操作的
     * */
    public List<MallIndexGoodsCategoryVO> getCategoriesForIndexDB() {
        List<MallIndexGoodsCategoryVO> categoryVOList = new ArrayList<>();
        List<MallGoodsCategory> allGoodsCategories = baseMapper.selectList(null);

        // 获取一级分类的固定数量的数据
        List<MallGoodsCategory> firstLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevelEnum.LEVEL_ONE.getLevel(), allGoodsCategories);
        
        if(!CollectionUtils.isEmpty(firstLevelCategories)){
            // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
            // List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();

            // 根据每个一级分类获取其二级分类数据
            for (MallGoodsCategory firstLevelCategory : firstLevelCategories) {
                // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
                List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();
                List<MallGoodsCategory> secondLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(firstLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_TWO.getLevel(),allGoodsCategories);
                MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();

                if(!CollectionUtils.isEmpty(secondLevelCategories)){
                    // MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                    // 根据每一个二级分类获取其三级分类数据
                    for (MallGoodsCategory secondLevelCategory : secondLevelCategories) {
                        MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                        List<MallGoodsCategory> thirdLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(secondLevelCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_THREE.getLevel(),allGoodsCategories);
                        //if(!CollectionUtils.isEmpty(thirdLevelCategories)){}

                        List<MallIndexThirdLevelCategoryVO> mallIndexThirdLevelCategoryVOS = BeanUtil.copyList(thirdLevelCategories, MallIndexThirdLevelCategoryVO.class);
                        BeanUtils.copyProperties(secondLevelCategory,mallIndexSecondLevelCategoryVO);
                        mallIndexSecondLevelCategoryVO.setThirdLevelCategoryVOS(mallIndexThirdLevelCategoryVOS);

                        mallIndexSecondLevelCategoryVOS.add(mallIndexSecondLevelCategoryVO);
                    }
                }

                // MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();
                BeanUtils.copyProperties(firstLevelCategory,mallIndexGoodsCategoryVO);
                mallIndexGoodsCategoryVO.setSecondLevelCategoryVOS(mallIndexSecondLevelCategoryVOS);

                categoryVOList.add(mallIndexGoodsCategoryVO);
            }
        }
        return categoryVOList;
    }

    /**
     * 获取商品分类部分信息显示给前台页面
     * */
    /*@Override
    public List<MallIndexGoodsCategoryVO> getCategoriesForIndex() {
        List<MallIndexGoodsCategoryVO> categoryVOList = new ArrayList<>();

        // 获取一级分类的固定数量的数据
        List<MallGoodsCategory> firstLevelCategories = this.getAppointedLevelGoodsCategories(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel());

        if(!CollectionUtils.isEmpty(firstLevelCategories)){
            // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
            // List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();

            // 根据每个一级分类获取其二级分类数据
            for (MallGoodsCategory firstLevelCategory : firstLevelCategories) {
                // 构建返回结果需要遍历两次，第一次遍历一级分类，第二次遍历二级分类
                List<MallIndexSecondLevelCategoryVO> mallIndexSecondLevelCategoryVOS = new ArrayList<>();
                List<MallGoodsCategory> secondLevelCategories = this.getAppointedLevelGoodsCategories(firstLevelCategory.getCategoryId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
                MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();

                if(!CollectionUtils.isEmpty(secondLevelCategories)){
                    // MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                    // 根据每一个二级分类获取其三级分类数据
                    for (MallGoodsCategory secondLevelCategory : secondLevelCategories) {
                        MallIndexSecondLevelCategoryVO mallIndexSecondLevelCategoryVO = new MallIndexSecondLevelCategoryVO();

                        List<MallGoodsCategory> thirdLevelCategories = this.getAppointedLevelGoodsCategories(secondLevelCategory.getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                        //if(!CollectionUtils.isEmpty(thirdLevelCategories)){}

                        List<MallIndexThirdLevelCategoryVO> mallIndexThirdLevelCategoryVOS = BeanUtil.copyList(thirdLevelCategories, MallIndexThirdLevelCategoryVO.class);
                        BeanUtils.copyProperties(secondLevelCategory,mallIndexSecondLevelCategoryVO);
                        mallIndexSecondLevelCategoryVO.setThirdLevelCategoryVOS(mallIndexThirdLevelCategoryVOS);

                        mallIndexSecondLevelCategoryVOS.add(mallIndexSecondLevelCategoryVO);
                    }
                }

                // MallIndexGoodsCategoryVO mallIndexGoodsCategoryVO = new MallIndexGoodsCategoryVO();
                BeanUtils.copyProperties(firstLevelCategory,mallIndexGoodsCategoryVO);
                mallIndexGoodsCategoryVO.setSecondLevelCategoryVOS(mallIndexSecondLevelCategoryVOS);

                categoryVOList.add(mallIndexGoodsCategoryVO);
            }
        }
        return categoryVOList;
    }*/

    /**
     *  代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，最后将方法的结果
     * 放入到缓存
     *  每一个需要缓存的数据都来指定要放到那个名字的缓存，推荐按照业务类型分。[缓存的分区(按照业务类型分)]
     *
     * 默认设置：
     *  如果缓存命中，方法不在执行
     *  key默认自动生成，缓存的名字::SimpleKey（自动生成的key值）
     *  缓存的value的值，默认使用JDK序列化机制，将序列化后的数据存到redis
     *  默认时间是-1
     *
     * 自定义设置：
     *  指定生成的缓存使用的key，key属性，接受一个SpEL表达式
     *  指定缓存的数据的存活机制，在配置文件中修改
     *  缓存的value保存为JSON格式，
     * */
    //@Cacheable({"category","goods"})
    @Cacheable(value = {"category"},key = "#root.methodName",sync = true) // SpEl表达式，如果普通字符串的一定要加单引号
    @Override
    public SearchPageCategoryVO getCategoriesForSearch(Long categoryId) {
        List<MallGoodsCategory> allGoodsCategories = baseMapper.selectList(null);

        SearchPageCategoryVO searchPageCategoryVO = new SearchPageCategoryVO();
        // 三级分类
        MallGoodsCategory thirdGoodsCategory = baseMapper.selectById(categoryId);
        if(thirdGoodsCategory != null && MallGoodsCategoryLevelEnum.LEVEL_THREE.getLevel().equals(thirdGoodsCategory.getCategoryLevel())){
            //获取当前三级分类的二级分类
            MallGoodsCategory secondGoodsCategory = baseMapper.selectById(thirdGoodsCategory.getParentId());
            if(secondGoodsCategory != null && MallGoodsCategoryLevelEnum.LEVEL_TWO.getLevel().equals(secondGoodsCategory.getCategoryLevel())){
                // 获取当前二级分类下的所有三级分类
                List<MallGoodsCategory> thirdLevelCategories = this.getAppointedLevelGoodsCategoriesFromAll(secondGoodsCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_THREE.getLevel(),allGoodsCategories);

                searchPageCategoryVO.setCurrentCategoryName(thirdGoodsCategory.getCategoryName());
                searchPageCategoryVO.setThirdLevelCategoryList(thirdLevelCategories);
                searchPageCategoryVO.setSecondLevelCategoryName(secondGoodsCategory.getCategoryName());

                // 获取当前二级分类的一级分类
                MallGoodsCategory firstGoodsCategory = baseMapper.selectById(secondGoodsCategory.getParentId());
                if(firstGoodsCategory != null && MallGoodsCategoryLevelEnum.LEVEL_ONE.getLevel().equals(firstGoodsCategory.getCategoryLevel())){
                    // 获取当前一级分类下的所有二级分类
                    List<MallGoodsCategory> secondLevelGoodsCategories = this.getAppointedLevelGoodsCategoriesFromAll(firstGoodsCategory.getCategoryId(), MallGoodsCategoryLevelEnum.LEVEL_TWO.getLevel(),allGoodsCategories);

                    searchPageCategoryVO.setFirstLevelCategoryName(firstGoodsCategory.getCategoryName());
                    searchPageCategoryVO.setSecondLevelCategoryList(secondLevelGoodsCategories);
                }
            }
        }

        return searchPageCategoryVO;
    }
}
