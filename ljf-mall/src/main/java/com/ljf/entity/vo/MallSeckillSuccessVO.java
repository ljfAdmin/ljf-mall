package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户秒杀成功VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "用户秒杀成功VO")
public class MallSeckillSuccessVO implements Serializable {
    private static final long serialVersionUID = 1503814153626594835L;

    @ApiModelProperty(value = "自增ID")
    private Long secId;

    /**
     * 额外字段
     * */
    private String md5;
}
