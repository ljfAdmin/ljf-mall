package com.ljf.service;

import com.ljf.entity.MallSeckill;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallSeckillSuccessVO;
import com.ljf.entity.vo.SeckillInterfaceExposeVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallSeckillService extends IService<MallSeckill> {

    boolean saveSeckill(MallSeckill mallSeckill);

    boolean updateSeckill(MallSeckill mallSeckill);

    SeckillInterfaceExposeVO exposerUrl(Long seckillId);

    MallSeckillSuccessVO executeSeckill(Long seckillId, Long userId) throws Exception;
}
