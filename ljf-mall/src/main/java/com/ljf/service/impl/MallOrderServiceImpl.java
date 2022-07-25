package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ljf.constant.*;
import com.ljf.constant.enums.MallOrderPayStatusEnum;
import com.ljf.constant.enums.MallOrderPayTypeEnum;
import com.ljf.constant.enums.MallOrderStatusEnum;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.*;
import com.ljf.entity.dto.StockNumDTO;
import com.ljf.entity.vo.MallOrderDetailVO;
import com.ljf.entity.vo.MallOrderItemVO;
import com.ljf.entity.vo.MallShoppingCartItemVO;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.mapper.MallOrderMapper;
import com.ljf.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.thread.OrderUnPaidTask;
import com.ljf.thread.TaskService;
import com.ljf.utils.BeanUtil;
import com.ljf.utils.NumberUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    @Autowired
    private MallOrderItemService mallOrderItemService;

    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Autowired
    private MallShoppingCartItemService mallShoppingCartItemService;

    @Autowired
    private MallUserCouponRecordService mallUserCouponRecordService;
    @Autowired
    private MallCouponService mallCouponService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MallSeckillSuccessService mallSeckillSuccessService;
    @Autowired
    private MallSeckillService mallSeckillService;

    @Override
    @Transactional
    public String checkDone(Long[] ids) {
        // 查询所有的订单
        List<MallOrder> orders = baseMapper.selectBatchIds(Arrays.asList(ids));
        String errorOrderNos = "";
        if(!CollectionUtils.isEmpty(orders)){
            for (MallOrder order : orders) {
                // is_delete字段为1的一定是已经关闭的订单
                /*if(order.getIsDeleted() == 1){
                    errorOrderNos += order.getOrderNo() + " ";
                    continue;
                }*/

                // 已经关闭或者已经完成无法关闭订单
                // 订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭
                if(order.getOrderStatus() == 4 || order.getOrderStatus() < 0){
                    errorOrderNos += order.getOrderNo() + " ";
                }
            }

            if(StringUtils.isEmpty(errorOrderNos)){
                // 订单状态正常 可以执行关闭操作 修改订单状态和更新时间
                for (MallOrder order : orders) {
                    // 商家关闭
                    order.setOrderStatus(MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus());
                    int updated = baseMapper.updateById(order);
                    if(updated <= 0){
                        return ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
                    }
                }
                return ToFrontMessageConstantEnum.SUCCESS.getResult();
            }else{// 订单此时不可执行关闭操作
                if(errorOrderNos.length() > 0 && errorOrderNos.length() < 100){
                    return errorOrderNos + "订单不能执行关闭操作";
                }else{
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }

        // 未查询到数据：返回错误提示
        return ToFrontMessageConstantEnum.DATA_NOT_EXIST.getResult();
    }


    /**
     * 出库
     * */
    @Override
    @Transactional
    public String checkOut(Long[] ids) {
        // 查询所有的订单
        List<MallOrder> orders = baseMapper.selectBatchIds(Arrays.asList(ids));
        String errorOrderNos = "";
        if(!CollectionUtils.isEmpty(orders)){
            for (MallOrder order : orders) {
                /*if(order.getIsDeleted().equals(1)){
                    errorOrderNos += order.getOrderNo() + " ";
                    continue;
                }*/

                // 1.已支付 2.配货完成
                if(order.getOrderStatus() != 1 && order.getOrderStatus() != 2){
                    errorOrderNos += order.getOrderNo() + " ";
                }
            }

            if (StringUtils.isEmpty(errorOrderNos)){
                // 订单状态正常 可以执行出库操作 修改订单状态和更新时间
                for (MallOrder order : orders) {
                    order.setOrderStatus(MallOrderStatusEnum.ORDER_EXPRESS.getOrderStatus());
                    int updated = baseMapper.updateById(order);
                    if(updated < 1){
                        return ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
                    }
                }
                return ToFrontMessageConstantEnum.SUCCESS.getResult();
            }else{
                // 订单此时不可执行出库操作，因为存在不满足要求订单号
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单的状态不是支付成功或配货完成无法执行出库操作";
                } else {
                    return "你选择了太多状态不是支付成功或配货完成的订单，无法执行出库操作";
                }
            }
        }

        // 说明没有查询到数据航
        return ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult();
    }

    /**
     * 关闭订单
     * */
    @Override
    @Transactional
    public String closeOrder(Long[] ids) {
        // 查询所有的订单
        List<MallOrder> orders = baseMapper.selectBatchIds(Arrays.asList(ids));
        String errorOrderNos = "";
        if(!CollectionUtils.isEmpty(orders)){
            for (MallOrder order : orders) {
                // is_deleted = 1，被删除订单，一定是关闭的
                /*if(order.getIsDeleted() == 1){
                    errorOrderNos += order.getOrderNo() + " ";
                    continue;
                }*/

                // 已经关闭的订单或者此时已经交易不能关闭的订单  4.交易成功
                // 订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功
                if(order.getOrderStatus() < 0 || order.getOrderStatus() == 4){
                    errorOrderNos += order.getOrderNo() + " ";
                }
            }

            if(StringUtils.isEmpty(errorOrderNos)){
                // 说明没有不符合要求的订单
                // 订单状态正常 可以执行关闭操作 修改订单状态和更新时间（这个MybatisPlus处理）
                for (MallOrder order : orders) {
                    order.setOrderStatus(MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus());
                    int updated = baseMapper.updateById(order);
                    if(updated < 1){// 更新失败
                        return ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
                    }
                }
                return ToFrontMessageConstantEnum.SUCCESS.getResult();
            }else{
                // 说明有不符合要求的订单，订单此时不可执行关闭操作
                if (errorOrderNos.length() > 0 && errorOrderNos.length() < 100) {
                    return errorOrderNos + "订单不能执行关闭操作";
                } else {
                    return "你选择的订单不能执行关闭操作";
                }
            }
        }

        // 没有查询到数据
        // 注：这里可能是没有记录，可以设置其他的枚举值
        return ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult();
    }

    /**
     * 根据订单号和用户ID获取订单详情VO
     *
     * 注：MallOrderDetailVO对标的是MallOrder类
     *
     * 注：除了订单号主键之外，订单号也是唯一的
     * */
    @Override
    public MallOrderDetailVO getOrderDetailVOByOrderNoAndUserId(String orderNo, Long userId) throws Exception {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder order = baseMapper.selectOne(queryWrapper);
        if(order == null){
            throw new Exception(ToFrontMessageConstantEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }

        //验证是否是当前userId下的订单，否则报错
        if(!userId.equals(order.getUserId())){
            throw new Exception(ToFrontMessageConstantEnum.NO_PERMISSION_ERROR.getResult());
        }

        QueryWrapper<MallOrderItem> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("order_id",order.getOrderId());
        List<MallOrderItem> orderItems = mallOrderItemService.list(queryWrapper1);

        //获取订单项数据
        if(CollectionUtils.isEmpty(orderItems)){
            throw new Exception(ToFrontMessageConstantEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }

        List<MallOrderItemVO> orderItemVOS = BeanUtil.copyList(orderItems, MallOrderItemVO.class);

        MallOrderDetailVO orderDetailVO = new MallOrderDetailVO();
        BeanUtils.copyProperties(order,orderDetailVO);
        orderDetailVO.setMallOrderItemVOS(orderItemVOS);
        orderDetailVO.setOrderStatusString(MallOrderStatusEnum.getMallOrderStatusEnumByStatus(order.getOrderStatus()).getName());
        orderDetailVO.setPayStatusString(MallOrderPayTypeEnum.getPayTypeEnumByType(order.getPayType()).getName());
        return orderDetailVO;
    }

    /**
     * 保存订单
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrder(MallUserVO user, Long couponUserId, List<MallShoppingCartItemVO> myShoppingCartItemVOS) throws Exception {
        if(!CollectionUtils.isEmpty(myShoppingCartItemVOS)){
            for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOS) {
                Long cartItemId = myShoppingCartItemVO.getCartItemId();
                Long goodsId = myShoppingCartItemVO.getGoodsId();

                MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
                // 如果不存在或者已经下架(商品上架状态 0-下架 1-上架)，则跳过
                if(goodsInfo == null || MallGoodsSellStatusConstant.SELL_STATUS_DOWN.equals(goodsInfo.getGoodsSellStatus())){
                    throw new Exception(goodsInfo.getGoodsName()+"已经下架，无法生成订单");
                }

                // 判断商品库存
                // 存在数量大于库存的情况，直接返回错误提醒
                if(myShoppingCartItemVO.getGoodsCount() > goodsInfo.getStockNum()){
                    throw new Exception(ToFrontMessageConstantEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
                }

                // 1.删除对应的购物车数据：根据每个购物车VO对象的ID
                boolean removed = mallShoppingCartItemService.removeById(myShoppingCartItemVO.getCartItemId());
                if(!removed){
                    throw new Exception(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
                }

                // 2.更新商品记录的库存
                StockNumDTO stockNumDTO = new StockNumDTO();
                BeanUtils.copyProperties(myShoppingCartItemVO,stockNumDTO);

                goodsInfo.setStockNum(goodsInfo.getStockNum() - stockNumDTO.getGoodsCount());

                UpdateWrapper<MallGoodsInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("goods_id",stockNumDTO.getGoodsId());
                updateWrapper.ge("stock_num",stockNumDTO.getGoodsCount());
                updateWrapper.eq("goods_sell_status",MallGoodsSellStatusConstant.SELL_STATUS_UP);

                boolean updated = mallGoodsInfoService.update(goodsInfo, updateWrapper);
                if(!updated){
                    throw new Exception(ToFrontMessageConstantEnum.SHOPPING_ITEM_COUNT_ERROR.getResult());
                }
            }


            // 生成订单号
            String orderNo = NumberUtil.genOrderNo();
            int priceTotal = 0;
            // 3.保存订单
            MallOrder order = new MallOrder();
            order.setOrderNo(orderNo);
            order.setUserId(user.getUserId());//用户主键
            order.setUserAddress(user.getAddress());//收货人地址
            // 收货人手机号、收货人姓名、
            order.setUserName(user.getNickName());
            order.setUserPhone(user.getLoginName());//登陆名称(默认为手机号)

            // 总价
            for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOS) {
                priceTotal += myShoppingCartItemVO.getGoodsCount() * myShoppingCartItemVO.getSellingPrice();
            }

            // 如果使用了优惠券：用户优惠券ID不为null
            if(couponUserId != null){
                MallUserCouponRecord userCouponRecord = mallUserCouponRecordService.getById(couponUserId);
                if(userCouponRecord != null){
                    MallCoupon mallCoupon = mallCouponService.getById(userCouponRecord.getCouponId());
                    // discount：@ApiModelProperty(value = "优惠金额，")  这里不能为负数！
                    if(mallCoupon != null && mallCoupon.getDiscount()!=null){
                        priceTotal -= mallCoupon.getDiscount();
                    }
                }
            }

            // 判断价格是否合理：实际上也是对上面优惠金额后价格的合理性做出判断
            // 这里姑且可以等于0，说明直接使用折扣可以抵消，当小于0的时候，说明折扣力度很大，
            // priceTotal设置为0；
            if(priceTotal < 0){
                priceTotal = 0;
            }
            order.setTotalPrice(priceTotal);

            // 注：这里定死了，就只能使用支付宝沙箱支付
            String extraInfo = "mall支付宝沙箱支付";
            order.setExtraInfo(extraInfo);

            // 保存订单项记录
            int inserted = baseMapper.insert(order);
            if(inserted < 1){
                throw new Exception(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
            }

            // 4.如果使用了优惠卷，则更新优惠卷状态
            if(couponUserId != null) {
                MallUserCouponRecord userCouponRecord = new MallUserCouponRecord();
                userCouponRecord.setCouponUserId(couponUserId);
                userCouponRecord.setOrderId(order.getOrderId());
                userCouponRecord.setUserId(user.getUserId());
                // @ApiModelProperty(value = "使用时间")
                userCouponRecord.setUsedTime(new Date());
                // @ApiModelProperty(value = "使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；")
                userCouponRecord.setUseStatus(1);
                boolean updated = mallUserCouponRecordService.updateById(userCouponRecord);
                if(!updated){// 更新失败
                    throw new Exception(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
                }
            }

            // 5.生成订单项快照，并保存数据库
            List<MallOrderItem> orderItems = new ArrayList<>();
            for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOS) {
                MallOrderItem orderItem = new MallOrderItem();
                // 对于其来说，除了自身主键和创建时间自动生成之外，其他比如订单ID、秒杀商品ID、关联商品ID、
                // 商品名称、商品主图、商品价格、商品数量

                // 使用BeanUtil工具类将myShoppingCartItemVO中的属性复制到 orderItem 对象中
                // 关联商品ID、商品数量、商品名称、商品主图、商品价格
                BeanUtils.copyProperties(myShoppingCartItemVO,orderItem);
                orderItem.setOrderId(order.getOrderId());
                orderItems.add(orderItem);
            }
            // 保存到数据库
            // 插入（批量），该方法不适合 Oracle，底层调用的是saveBatch(orderItems,30)的方法
            boolean saved = mallOrderItemService.saveBatch(orderItems);
            if(!saved){
                throw new Exception(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
            }

            // 6.订单支付超期任务，超过300秒自动取消订单
            taskService.addTask(new OrderUnPaidTask(order.getOrderId(), FrontMallOrderInfoConstant.ORDER_UNPAID_OVER_TIME * 1000));
            // 所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
            return orderNo;
        }

        return null;
    }

    /**
     * 秒杀情况下订单的保存
     * */
    @Override
    public String seckillSaveOrder(Long seckillSuccessId, Long userId) throws Exception {
        MallSeckillSuccess seckillSuccess = mallSeckillSuccessService.getById(seckillSuccessId);
        if(seckillSuccess == null)
            throw new Exception("秒杀成功信息不存在");
        if(!userId.equals(seckillSuccess.getUserId()))
            throw new Exception("当前登陆用户与抢购秒杀商品的用户不匹配");

        Long seckillId = seckillSuccess.getSeckillId();
        MallSeckill seckill = mallSeckillService.getById(seckillId);
        Long goodsId = seckill.getGoodsId();
        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);

        // 生成订单号
        String orderNo = NumberUtil.genOrderNo();
        // 保存订单
        MallOrder mallOrder = new MallOrder();
        mallOrder.setOrderNo(orderNo);
        mallOrder.setTotalPrice(seckill.getSeckillPrice());
        mallOrder.setUserId(userId);
        mallOrder.setUserAddress("秒杀测试地址");
        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        mallOrder.setPayStatus(MallOrderPayStatusEnum.PAY_SUCCESS.getPayStatus());
        mallOrder.setPayType(MallOrderPayTypeEnum.WEIXIN_PAY.getPayType());
        mallOrder.setPayTime(new Date());

        String extraInfo = "";
        mallOrder.setExtraInfo(extraInfo);

        if(baseMapper.insert(mallOrder) <= 0){
            throw new Exception("生成订单内部异常");
        }

        // 保存订单商品项 OrderItem
        MallOrderItem orderItem = new MallOrderItem();
        orderItem.setOrderId(mallOrder.getOrderId());
        orderItem.setSeckillId(seckillId);
        orderItem.setGoodsId(goodsInfo.getGoodsId());
        orderItem.setGoodsName(goodsInfo.getGoodsName());
        orderItem.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
        orderItem.setSellingPrice(goodsInfo.getSellingPrice());
        // @ApiModelProperty(value = "订单中商品数量(订单快照)")
        orderItem.setGoodsCount(1);
        if(!mallOrderItemService.save(orderItem)){
            throw new Exception("生成订单内部异常");
        }

        // 订单支付超期任务
        taskService.addTask(new OrderUnPaidTask(mallOrder.getOrderId(), 30 * 1000));
        return orderNo;
    }

    /**
     * 根据订单号和用户Id获取订单详情VO对象
     * */
    @Override
    public MallOrderDetailVO getOrderDetailByOrderNo(String orderNo, Long userId) throws Exception {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder mallOrder = baseMapper.selectOne(queryWrapper);
        if(mallOrder == null){
            throw new Exception(ToFrontMessageConstantEnum.ORDER_NOT_EXIST_ERROR.getResult());
        }
        //验证是否是当前userId下的订单，否则报错，这里实际上已经无需判断
        if (!userId.equals(mallOrder.getUserId())) {
            throw new Exception(ToFrontMessageConstantEnum.NO_PERMISSION_ERROR.getResult());
        }

        QueryWrapper<MallOrderItem> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("order_id",mallOrder.getOrderId());
        List<MallOrderItem> orderItems = mallOrderItemService.list(queryWrapper1);
        //获取订单项数据
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new Exception(ToFrontMessageConstantEnum.ORDER_ITEM_NOT_EXIST_ERROR.getResult());
        }

        List<MallOrderItemVO> orderItemVOS = BeanUtil.copyList(orderItems, MallOrderItemVO.class);
        MallOrderDetailVO orderDetailVO = new MallOrderDetailVO();
        BeanUtils.copyProperties(mallOrder,orderDetailVO);
        orderDetailVO.setOrderStatusString(MallOrderStatusEnum.getMallOrderStatusEnumByStatus(mallOrder.getOrderStatus()).getName());
        orderDetailVO.setPayStatusString(MallOrderPayStatusEnum.getPayStatusEnumByStatus(mallOrder.getPayStatus()).getName());
        orderDetailVO.setMallOrderItemVOS(orderItemVOS);

        return orderDetailVO;
    }

    @Override
    public String paySuccess(String orderNo, Integer payType) {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder mallOrder = baseMapper.selectOne(queryWrapper);

        if (mallOrder == null) {// 订单不存在
            return ToFrontMessageConstantEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }

        // 订单状态判断 非待支付状态下不进行修改操作
        if (!MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus().equals(mallOrder.getOrderStatus())) {
            return ToFrontMessageConstantEnum.ORDER_STATUS_ERROR.getResult();
        }

        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        mallOrder.setPayType(payType);
        mallOrder.setPayStatus(MallOrderPayStatusEnum.PAY_SUCCESS.getPayStatus());
        mallOrder.setPayTime(new Date());
        int updated = baseMapper.updateById(mallOrder);
        if(updated <= 0){
            return ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
        }

        taskService.removeTask(new OrderUnPaidTask(mallOrder.getOrderId()));
        return ToFrontMessageConstantEnum.SUCCESS.getResult();

    }

    /**
     * 手动取消订单
     * */
    @Override
    public String cancelOrder(String orderNo, Long userId) {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder mallOrder = baseMapper.selectOne(queryWrapper);

        if (mallOrder == null) {// 要取消的订单不存在
            return ToFrontMessageConstantEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }

        // 判断是否是当前userId下的订单，否则报错
        if(!userId.equals(mallOrder.getUserId())){
            return ToFrontMessageConstantEnum.NO_PERMISSION_ERROR.getResult();
        }

        // 订单状态判断，如果订单交易成功或者被关闭，则无法取消订单
        // 订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭
        if(MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus().equals(mallOrder.getOrderStatus())
                || MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus().equals(mallOrder.getOrderStatus())
                || MallOrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus().equals(mallOrder.getOrderStatus())
                || MallOrderStatusEnum.ORDER_CLOSED_BY_JUDGE.getOrderStatus().equals(mallOrder.getOrderStatus())){
            return ToFrontMessageConstantEnum.ORDER_STATUS_ERROR.getResult();
        }

        // 执行关闭订单的操作
        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
        int updated = baseMapper.updateById(mallOrder);
        return updated > 0 ? ToFrontMessageConstantEnum.SUCCESS.getResult() : ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
    }

    /**
     * 确认收货(订单)
     * */
    @Override
    public String finishOrder(String orderNo, Long userId) {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder mallOrder = baseMapper.selectOne(queryWrapper);

        if(mallOrder == null){
            return ToFrontMessageConstantEnum.ORDER_NOT_EXIST_ERROR.getResult();
        }

        // 验证是否是当前userId下的订单，否则报错
        if (!userId.equals(mallOrder.getUserId())) {
            return ToFrontMessageConstantEnum.NO_PERMISSION_ERROR.getResult();
        }

        // 订单状态判断 非出库状态下不进行修改操作
        if(!MallOrderStatusEnum.ORDER_EXPRESS.getOrderStatus().equals(mallOrder.getOrderStatus())){
            return ToFrontMessageConstantEnum.ORDER_STATUS_ERROR.getResult();
        }

        // 确认收货/订单操作
        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
        int updated = baseMapper.updateById(mallOrder);

        return updated > 0 ? ToFrontMessageConstantEnum.SUCCESS.getResult() : ToFrontMessageConstantEnum.UPDATED_FAILED.getResult();
    }


}
