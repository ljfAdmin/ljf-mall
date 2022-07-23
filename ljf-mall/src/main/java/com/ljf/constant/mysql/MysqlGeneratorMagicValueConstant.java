package com.ljf.constant.mysql;

import java.util.ArrayList;
import java.util.List;

public class MysqlGeneratorMagicValueConstant {
    public static final String OUTPUT_DIR = "E:\\MySpace\\ljf-mall\\ljf-mall";
    public static final String OUTPUT_DIR_MODULE = "/src/main/java";
    public static final String AUTHOR = "ljf";
    public static final String SERVICE_NAME = "%sService";
    public static final String MYSQL_URL = "jdbc:mysql://localhost:3306/ljf_mall?serverTimezone=GMT%2B8";
    public static final String MYSQL_DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    public static final String MYSQL_USERNAME = "root";
    public static final String MYSQL_PASSWORD = "ljfmysql0107";
    public static final String PARENT_PACKAGE = "com.ljf";
    public static final String MODULE_NAME = "";
    public static final String CONTROLLER = "controller";
    public static final String ENTITY = "entity";
    public static final String SERVICE = "service";
    public static final String MAPPER = "mapper";
    public static final List<String> TABLE_NAMES = new ArrayList<>();

    static {
        TABLE_NAMES.add("mall_admin_user");
        TABLE_NAMES.add("mall_carousel");
        TABLE_NAMES.add("mall_coupon");
        TABLE_NAMES.add("mall_goods_category");
        TABLE_NAMES.add("mall_goods_info");
        TABLE_NAMES.add("mall_index_config");
        TABLE_NAMES.add("mall_order");
        TABLE_NAMES.add("mall_order_item");
        TABLE_NAMES.add("mall_seckill");
        TABLE_NAMES.add("mall_seckill_success");
        TABLE_NAMES.add("mall_shopping_cart_item");
        TABLE_NAMES.add("mall_user");
        TABLE_NAMES.add("mall_user_coupon_record");
    }

}
