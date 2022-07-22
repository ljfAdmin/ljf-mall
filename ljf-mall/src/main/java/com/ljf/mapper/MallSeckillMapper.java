package com.ljf.mapper;

import com.ljf.entity.MallSeckill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallSeckillMapper extends BaseMapper<MallSeckill> {

    void killByProcedure(Map<String, Object> map);
}
