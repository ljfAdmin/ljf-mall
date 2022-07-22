package com.ljf.constant;

public class MallGoodsSellStatusConstant {
    private MallGoodsSellStatusConstant(){}

    public final static Integer SELL_STATUS_UP = 1;//商品上架架状态
    public final static Integer SELL_STATUS_DOWN = 0;//商品下架状态

    /**
     * 判断输入的状态值是否合法
     *  如果返回true：合法
     *  如果返回false：不合法
     * */
    public static boolean judgeStatusIsLegal(Integer goodsSellStatus){
        return !(!SELL_STATUS_DOWN.equals(goodsSellStatus) && !SELL_STATUS_UP.equals(goodsSellStatus));
    }
}
