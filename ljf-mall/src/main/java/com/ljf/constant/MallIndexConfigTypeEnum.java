package com.ljf.constant;

public enum  MallIndexConfigTypeEnum {
    DEFAULT(0, "DEFAULT"),
    // 搜索框热搜
    SEARCH_BINDEX_RECOMMEND_FOR_YOUOX_HOT_SEARCH(1, "INDEX_SEARCH_HOTS"),
    // 搜索下拉框热搜
    SEARCH_DROP_DOWN_BOX_HOT_SEARCH(2, "INDEX_SEARCH_DOWN_HOTS"),
    // (首页)热销商品
    INDEX_HOT_SELLING_GOODS(3, "INDEX_GOODS_HOTS"),
    // (首页)新品上线
    INDEX_NEW_GOODS(4, "INDEX_GOODS_NEW"),
    // (首页)为你推荐
    INDEX_RECOMMEND_FOR_YOU(5, "INDEX_GOODS_RECOMMEND");

    private Integer configType;
    private String name;

    private MallIndexConfigTypeEnum(Integer configType,String name){
        this.configType = configType;
        this.name = name;
    }

    public static MallIndexConfigTypeEnum getMallIndexConfigTypeEnumByType(Integer type) {
        for (MallIndexConfigTypeEnum mallIndexConfigTypeEnum : MallIndexConfigTypeEnum.values()) {
            if (mallIndexConfigTypeEnum.getConfigType().equals(type)) {
                return mallIndexConfigTypeEnum;
            }
        }
        return DEFAULT;
    }

    public Integer getConfigType() {
        return configType;
    }

    public void setConfigType(Integer configType) {
        this.configType = configType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MallIndexConfigTypeEnum{" +
                "configType=" + configType +
                ", name='" + name + '\'' +
                '}';
    }

}
