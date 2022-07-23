package com.ljf.constant.enums;

public enum MallGoodsCategoryLevelEnum {
    LEVEL_ONE(1, "一级分类"),
    LEVEL_TWO(2, "二级分类"),
    LEVEL_THREE(3, "三级分类");

    private Integer level;
    private String description;

    private MallGoodsCategoryLevelEnum(Integer level, String description){
        this.level = level;
        this.description = description;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
