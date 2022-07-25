package com.ljf.controller.front;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.annotations.RepeatSubmit;
import com.ljf.constant.*;
import com.ljf.constant.aliyun.AliyunPropertiesConstant;
import com.ljf.constant.enums.MallOrderPayStatusEnum;
import com.ljf.constant.enums.MallOrderStatusEnum;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallOrder;
import com.ljf.entity.MallOrderItem;
import com.ljf.entity.vo.*;
import com.ljf.service.MallOrderItemService;
import com.ljf.service.MallOrderService;
import com.ljf.service.MallShoppingCartItemService;
import com.ljf.service.MallUserService;
import com.ljf.utils.MD5Util;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class FrontMallOrderController {
    private static Logger log = LoggerFactory.getLogger(FrontMallOrderController.class);

    @Autowired
    private MallOrderService mallOrderService;

    @Autowired
    private MallOrderItemService mallOrderItemService;

    @Autowired
    private MallUserService mallUserService;

    @Autowired
    private MallShoppingCartItemService mallShoppingCartItemService;

    /**
     * 根据订单号跳转到订单详情页面
     * */
    @GetMapping("/orders/{orderNo}")
    public String orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession httpSession) throws Exception {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        MallOrderDetailVO orderDetailVO = mallOrderService.getOrderDetailVOByOrderNoAndUserId(orderNo, user.getUserId());
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    /**
     * 跳转到订单列表页面，并携带请求参数
     * */
    @GetMapping("/orders")
    public String orderListPage(@RequestParam Map<String, Object> params, HttpServletRequest request, HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        params.put("userId", user.getUserId());

        if (StringUtils.isEmpty((CharSequence) params.get("page"))) {
            params.put("page", 1);
        }
        params.put("limit", FrontMallOrderInfoConstant.ORDER_SEARCH_PAGE_LIMIT);

        //封装我的订单数据
        Integer currentPage = (Integer) params.get("page");
        Integer limit = (Integer) params.get("limit");
        Page page = new Page(currentPage,limit);// 自动类型转换

        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        String orderNo = (String) params.get("orderNo");
        if(!StringUtils.isEmpty(orderNo)){
            queryWrapper.eq("order_no",orderNo);
        }
        queryWrapper.eq("user_id",user.getUserId());
        Integer payType = (Integer) params.get("payType");
        if(!Objects.isNull(payType)){
            queryWrapper.eq("pay_type",payType);
        }
        Integer orderStatus = (Integer) params.get("orderStatus");
        if(!Objects.isNull(orderStatus)){
            queryWrapper.eq("order_status",orderStatus);
        }
        queryWrapper.gt("create_time",(Date) params.get("startTime"));
        queryWrapper.lt("create_time",(Date) params.get("endTime"));
        queryWrapper.orderByDesc("create_time");
        Integer start = (currentPage - 1) * limit;
        queryWrapper.last("limit "+start+","+limit);

        mallOrderService.page(page,queryWrapper);
        List<MallOrderListVO> orderListVOS = new ArrayList<>();
        List records = page.getRecords();
        // 每个record都是MallOrder对象，这里要将其转换为MallOrderListVO对象
        for (Object record : records) {
            MallOrder order = (MallOrder) record;
            MallOrderListVO orderListVO = new MallOrderListVO();
            BeanUtils.copyProperties(order,orderListVO);
            orderListVO.setOrderStatusString(MallOrderStatusEnum.getMallOrderStatusEnumByStatus(order.getOrderStatus()).getName());

            QueryWrapper<MallOrderItem> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("order_id",order.getOrderId());
            queryWrapper.orderByDesc("create_time");
            List<MallOrderItem> orderItems = mallOrderItemService.list(queryWrapper1);

            List<MallOrderItemVO> orderItemVOS = new ArrayList<>();
            if(!CollectionUtils.isEmpty(orderItems)){
                for (MallOrderItem orderItem : orderItems) {
                    MallOrderItemVO mallOrderItemVO = new MallOrderItemVO();
                    BeanUtils.copyProperties(orderItem,mallOrderItemVO);
                    orderItemVOS.add(mallOrderItemVO);
                }
            }
            orderListVO.setMallOrderItemVOS(orderItemVOS);

            orderListVOS.add(orderListVO);
        }

        page.setRecords(orderListVOS);

        request.setAttribute("orderPageResult", page);
        request.setAttribute("path", "orders");
        return "mall/my-orders";
    }

    /**
     * 保存订单
     * */
    @RepeatSubmit
    @GetMapping("/saveOrder")
    // public String saveOrder(String couponUserId, HttpSession httpSession) throws Exception {
    public String saveOrder(Long couponUserId, HttpSession httpSession) throws Exception {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        List<MallShoppingCartItemVO> myShoppingCartItemVOS = mallShoppingCartItemService.getMyShoppingCartItems(user.getUserId());

        if (StringUtils.isEmpty(user.getAddress().trim())) {
            //无收货地址
            throw new Exception(ToFrontMessageConstantEnum.NULL_ADDRESS_ERROR.getResult());
        }

        if (CollectionUtils.isEmpty(myShoppingCartItemVOS)) {
            //购物车中无数据则跳转至错误页
            throw new Exception(ToFrontMessageConstantEnum.SHOPPING_ITEM_ERROR.getResult());
        }

        //保存订单并返回订单号
        //String saveOrderResult = mallOrderService.saveOrder(user, Long.valueOf(couponUserId), myShoppingCartItemVOS);
        String saveOrderResult = mallOrderService.saveOrder(user, couponUserId, myShoppingCartItemVOS);

        //跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    /**
     * 保存订单，秒杀情况下的保存
     *  seckillSuccessId:秒杀成功的ID
     *  userId:用户ID
     *
     * */
    @RepeatSubmit
    @GetMapping("/saveSeckillOrder/{seckillSuccessId}/{userId}/{seckillSecretKey}")
    public String saveOrder(@PathVariable("seckillSuccessId") Long seckillSuccessId,
                            @PathVariable("userId") Long userId,
                            @PathVariable("seckillSecretKey") String seckillSecretKey) throws Exception {

        if (seckillSecretKey == null || !seckillSecretKey.equals(MD5Util.encrypt(seckillSuccessId + MallSeckillInfoConstant.SECKILL_ORDER_SALT))) {
            throw new Exception("秒杀商品下单不合法");
        }

        // 保存订单并返回订单号
        String saveOrderResult = mallOrderService.seckillSaveOrder(seckillSuccessId, userId);
        // 跳转到订单详情页
        return "redirect:/orders/" + saveOrderResult;
    }

    /**
     * 跳转到选择支付类型的页面
     *
     * 自定义防止重复提交 注解
     * */
    @RepeatSubmit
    @GetMapping("/selectPayType")
    public String selectPayType(HttpServletRequest request, @RequestParam("orderNo") String orderNo, HttpSession httpSession) throws Exception {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        MallOrder mallOrder = judgeOrderUserId(orderNo, user.getUserId());

        //判断订单状态
        if (mallOrder.getOrderStatus().intValue() != MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new Exception(ToFrontMessageConstantEnum.ORDER_STATUS_ERROR.getResult());
        }

        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", mallOrder.getTotalPrice());
        return "mall/pay-select";
    }
    /**
     * 判断订单关联用户id和当前登陆用户是否一致
     *
     * @param orderNo 订单编号
     * @param userId  用户ID
     * @return 验证成功后返回订单对象
     */
    private MallOrder judgeOrderUserId(String orderNo, Long userId) throws Exception {
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no",orderNo);
        MallOrder mallOrder = mallOrderService.getOne(queryWrapper);

        // 判断订单userId
        if (mallOrder == null || !userId.equals(mallOrder.getUserId())) {
            throw new Exception("当前订单用户异常");
        }
        return mallOrder;
    }

    /**
     * 跳转到支付页面
     * */
    @RepeatSubmit
    @GetMapping("/payPage")
    public String payOrder(HttpServletRequest request,
                           @RequestParam("orderNo") String orderNo,
                           HttpSession httpSession,
                           @RequestParam("payType") int payType) throws Exception {

        MallUserVO mallUserVO = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        Long userId = mallUserVO.getUserId();
        // 判断该订单是否是属于当前用户
        MallOrder mallOrder = judgeOrderUserId(orderNo, userId);

        // 判断订单userId，再次判断
        if (!userId.equals(mallOrder.getUserId())) {
            throw new Exception(ToFrontMessageConstantEnum.NO_PERMISSION_ERROR.getResult());
        }

        // 判断订单状态
        // 订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭
        if (!MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus().equals(mallOrder.getOrderStatus())
                || mallOrder.getPayStatus() != MallOrderPayStatusEnum.PAY_ING.getPayStatus()) {
            throw new Exception("订单结算异常");
        }

        request.setAttribute("orderNo", orderNo);
        request.setAttribute("totalPrice", mallOrder.getTotalPrice());

        if (payType == 1) {// 支付宝支付
            // 示例代码：https://opendocs.alipay.com/open/02np94
            request.setCharacterEncoding(CommonConstant.UTF_ENCODING);
            // 实例化客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    AliyunPropertiesConstant.GATEWAY,
                    AliyunPropertiesConstant.APP_ID,
                    AliyunPropertiesConstant.RSA_PRIVATE_KEY,
                    AliyunPropertiesConstant.FORMAT,
                    AliyunPropertiesConstant.CHARSET,
                    AliyunPropertiesConstant.ALIPAY_PUBLIC_KEY,
                    AliyunPropertiesConstant.SIGN_TYPE);

            // 实例化具体API对应的request类,类名称和接口名称对应,
            // 当前调用接口名称：alipay.open.public.template.message.industry.modify
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            // 在公共参数中设置回跳和通知地址,通知地址需要公网可访问
            // String url = CommonConstant.SERVER_URL + request.getContextPath();
            String url = CommonConstant.SERVER_URL;
            alipayRequest.setReturnUrl(url + "returnOrders/" + mallOrder.getOrderNo() + "/" + userId);
            alipayRequest.setNotifyUrl(url + "paySuccess?payType=1&orderNo=" + mallOrder.getOrderNo());

            // 填充业务参数

            // 必填
            // 商户订单号，需保证在商户端不重复
            String out_trade_no = mallOrder.getOrderNo() + new Random().nextInt(9999);
            // 销售产品码，与支付宝签约的产品码名称。目前仅支持FAST_INSTANT_TRADE_PAY
            String product_code = "FAST_INSTANT_TRADE_PAY";
            // 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
            String total_amount = String.valueOf(mallOrder.getTotalPrice());
            // 订单标题
            String subject = "支付宝测试";

            // 选填
            // 商品描述，可空
            String body = "商品描述";

            // SDK已经封装掉了公共参数，这里只需要传入业务参数
            // 此次只是参数展示，未进行字符串转义，实际情况下请转义
            alipayRequest.setBizContent("{" + "\"out_trade_no\":\"" + out_trade_no + "\"," + "\"product_code\":\""
                    + product_code + "\"," + "\"total_amount\":\"" + total_amount + "\"," + "\"subject\":\"" + subject
                    + "\"," + "\"body\":\"" + body + "\"}");
            // 请求
            String form;
            try {
                // 需要自行申请支付宝的沙箱账号、申请appID，并在配置文件中依次配置AppID、密钥、公钥，否则这里会报错。
                form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
                request.setAttribute("form", form);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
            return "mall/alipay";
        } else { // 微信支付
            return "mall/wxpay";
        }
    }

    /**
     * 返回订单
     * */
    @GetMapping("/returnOrders/{orderNo}/{userId}")
    public String returnOrderDetailPage(HttpServletRequest request,
                                        @PathVariable String orderNo,
                                        @PathVariable Long userId) throws Exception {
        log.info("支付宝return通知数据记录：orderNo: {}, 当前登陆用户：{}", orderNo, userId);
        MallOrder mallOrder = this.judgeOrderUserId(orderNo, userId);
        // 将notifyUrl中逻辑放到此处：未支付订单更新订单状态
        // 订单状态不等于待支付  或者  订单支付状态不等于支付中
        /*if(!MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus().equals(mallOrder.getOrderStatus())
                || MallOrderPayStatusEnum.PAY_ING.getPayStatus() != mallOrder.getPayStatus()){
            throw new Exception("订单关闭异常");
        }

        mallOrder.setOrderStatus(MallOrderStatusEnum.ORDER_PAID.getOrderStatus());
        mallOrder.setPayType(MallOrderPayTypeEnum.ALI_PAY.getPayType());
        mallOrder.setPayStatus(MallOrderPayStatusEnum.PAY_SUCCESS.getPayStatus());
        mallOrder.setPayTime(new Date());

        if(!mallOrderService.updateById(mallOrder)){
            return "error/error_5xx";
        }*/

        // 通过订单号获取订单详情VO信息
        MallOrderDetailVO orderDetailVO = mallOrderService.getOrderDetailByOrderNo(orderNo, userId);

        if (orderDetailVO == null) {
            return "error/error_5xx";
        }
        request.setAttribute("orderDetailVO", orderDetailVO);
        return "mall/order-detail";
    }

    /**
     * 支付成功后
     * */
    @PostMapping("/paySuccess")
    @ResponseBody
    public Result paySuccess(Integer payType, String orderNo) {
        log.info("支付宝paySuccess通知数据记录：orderNo: {}, payType：{}", orderNo, payType);
        String payResult = mallOrderService.paySuccess(orderNo, payType);
        if (ToFrontMessageConstantEnum.SUCCESS.getResult().equals(payResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(payResult);
        }
    }

    /**
     * 取消订单
     * */
    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public Result cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        String cancelOrderResult = mallOrderService.cancelOrder(orderNo, user.getUserId());
        if (ToFrontMessageConstantEnum.SUCCESS.getResult().equals(cancelOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(cancelOrderResult);
        }
    }

    /**
     * 确认收货(订单)
     * */
    @RepeatSubmit
    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public Result finishOrder(@PathVariable("orderNo") String orderNo, HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);

        String finishOrderResult = mallOrderService.finishOrder(orderNo, user.getUserId());
        if (ToFrontMessageConstantEnum.SUCCESS.getResult().equals(finishOrderResult)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult(finishOrderResult);
        }
    }



}
