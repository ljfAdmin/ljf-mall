package com.ljf.entity.vo;

import com.ljf.constant.FrontSeckillStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀服务接口地址暴露类
 * */
@Data
@ApiModel(value = "秒杀服务接口地址暴露类")
public class SeckillInterfaceExposeVO implements Serializable {
    private static final long serialVersionUID = -7615136662052646516L;

    // 秒杀状态enum
    private FrontSeckillStatusEnum frontSeckillStatusEnum;

    // 一种加密措施
    private String md5;

    // id
    @ApiModelProperty(value = "自增ID")
    private Long seckillId;

    // 系统当前时间（毫秒）
    private long now;

    // 开启时间
    private long start;

    // 结束时间
    private long end;

    public SeckillInterfaceExposeVO(FrontSeckillStatusEnum seckillStatusEnum, long seckillId, String md5) {
        this.frontSeckillStatusEnum = seckillStatusEnum;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    public SeckillInterfaceExposeVO(FrontSeckillStatusEnum seckillStatusEnum, long seckillId, long now, long start, long end) {
        this.frontSeckillStatusEnum = seckillStatusEnum;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    public SeckillInterfaceExposeVO(FrontSeckillStatusEnum seckillStatusEnum, long seckillId) {
        this.frontSeckillStatusEnum = seckillStatusEnum;
        this.seckillId = seckillId;
    }
}
