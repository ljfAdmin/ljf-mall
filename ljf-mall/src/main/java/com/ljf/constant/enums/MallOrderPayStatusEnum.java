package com.ljf.constant.enums;

/**
 * 订单状态:0.支付中 1.支付成功 -1.支付失败
 * */
public enum MallOrderPayStatusEnum {
    DEFAULT(-1, "支付失败"),
    PAY_ING(0, "支付中"),
    PAY_SUCCESS(1, "支付成功");

    private Integer payStatus;

    private String name;

    private MallOrderPayStatusEnum(int payStatus, String name) {
        this.payStatus = payStatus;
        this.name = name;
    }

    public static MallOrderPayStatusEnum getPayStatusEnumByStatus(int payStatus) {
        for (MallOrderPayStatusEnum payStatusEnum : MallOrderPayStatusEnum.values()) {
            if (payStatusEnum.getPayStatus() == payStatus) {
                return payStatusEnum;
            }
        }
        return DEFAULT;
    }

    public int getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(int payStatus) {
        this.payStatus = payStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
