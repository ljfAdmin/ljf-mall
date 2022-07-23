package com.ljf.constant.enums;

/**
 * 订单状态:0.无 1.支付宝 2.微信支付
 * */
public enum MallOrderPayTypeEnum {
    DEFAULT(-1, "ERROR"),
    NOT_PAY(0, "无"),
    ALI_PAY(1, "支付宝"),
    WEIXIN_PAY(2, "微信支付");

    private Integer payType;

    private String name;

    private MallOrderPayTypeEnum(int payType, String name) {
        this.payType = payType;
        this.name = name;
    }

    public static MallOrderPayTypeEnum getPayTypeEnumByType(int payType) {
        for (MallOrderPayTypeEnum payTypeEnum : MallOrderPayTypeEnum.values()) {
            if (payTypeEnum.getPayType() == payType) {
                return payTypeEnum;
            }
        }
        return DEFAULT;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
