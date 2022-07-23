package com.ljf.constant.enums;

public enum ToFrontMessageConstantEnum {
    SUCCESS("success"),
    SAVE_FAILED("添加记录失败"),
    UPDATED_FAILED("修改记录失败"),
    SELECT_DETAIL_FAILED("查询失败"),
    DELETE_FAILED("删除记录失败"),
    PLEASE_INPUT_REQUIRED_PARAM("请输入必要的参数"),
    INPUT_PARAM_EXCEPTION("传入参数异常"),
    DATA_NOT_EXIST("未查询到数据"),

    GOODS_INFO_SELL_STATUS_EXCEPTION("商品状态异常"),
    GOODS_NOT_EXIST("商品不存在！"),
    SHOPPING_ITEM_COUNT_ERROR("商品库存不足！"),

    ORDER_NOT_EXIST_ERROR("订单不存在！"),
    ORDER_ITEM_NOT_EXIST_ERROR("订单项不存在！"),
    ORDER_STATUS_ERROR("订单状态异常！"),

    NULL_ADDRESS_ERROR("地址不能为空！"),
    SHOPPING_ITEM_ERROR("购物车数据异常！"),

    NO_PERMISSION_ERROR("无权限！"),

    LOGIN_NAME_NULL("请输入登录名！"),
    LOGIN_PASSWORD_NULL("请输入密码！"),
    LOGIN_VERIFY_CODE_NULL("请输入验证码！"),
    LOGIN_VERIFY_CODE_ERROR("验证码错误！"),
    LOGIN_USER_LOCKED("用户已被禁止登录！"),
    LOGIN_ERROR("登录失败！"),
    SAME_LOGIN_NAME_EXIST("用户名已存在！"),

    SECKILL_GOODS_INFO_NOT_EXIST("秒杀商品信息不存在"),
    SECKILL_INFO_NOT_EXIST("秒杀信息不存在"),
    SECKILL_INFO_OR_GOODS_INFO_NOT_EXIST("秒杀商品信息不存在或者秒杀信息本身不存在"),

    MALL_USER_LOCKED_FLAG_UPDATED_FAILURE("MallUser的锁定状态设置失败");

    private String result;

    private ToFrontMessageConstantEnum(String result){
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
