package com.ljf.service;

import com.ljf.entity.MallOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallOrderDetailVO;
import com.ljf.entity.vo.MallShoppingCartItemVO;
import com.ljf.entity.vo.MallUserVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallOrderService extends IService<MallOrder> {

    /**
     * 检查订单是否完成，并关闭
     * */
    String checkDone(Long[] ids);

    /**
     * 出库
     * */
    String checkOut(Long[] ids);

    /**
     * 关闭订单
     * */
    String closeOrder(Long[] ids);

    /**
     * 根据订单号和用户ID获取订单详情VO
     * */
    MallOrderDetailVO getOrderDetailVOByOrderNoAndUserId(String orderNo, Long userId) throws Exception;

    /**
     * 保存订单
     * */
    String saveOrder(MallUserVO user, Long couponUserId, List<MallShoppingCartItemVO> myShoppingCartItemVOS) throws Exception;

    /**
     * 秒杀情况下订单的保存
     * */
    String seckillSaveOrder(Long seckillSuccessId, Long userId) throws Exception;

    /**
     * 根据订单号和用户Id获取订单详情VO对象
     * */
    MallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) throws Exception;

    String paySuccess(String orderNo, Integer payType);

    /**
     * 手动取消订单
     * */
    String cancelOrder(String orderNo, Long userId);

    /**
     * 确认收货(订单)
     * */
    String finishOrder(String orderNo, Long userId);
}
