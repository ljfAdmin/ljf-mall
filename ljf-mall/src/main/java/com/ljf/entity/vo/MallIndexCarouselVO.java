package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页轮播图VO
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "首页轮播图VO")
public class MallIndexCarouselVO implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "轮播图")
    private String carouselUrl;
    @ApiModelProperty(value = "点击后的跳转地址(默认不跳转)")
    private String redirectUrl;
}
