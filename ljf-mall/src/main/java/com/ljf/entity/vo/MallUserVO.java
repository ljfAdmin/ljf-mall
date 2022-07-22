package com.ljf.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="MallUserVO对象", description="前台页面用户信息封装")
public class MallUserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户主键id")
    private Long userId;
    @ApiModelProperty(value = "用户昵称")
    private String nickName;
    @ApiModelProperty(value = "登陆名称(默认为手机号)")
    private String loginName;
    @ApiModelProperty(value = "个性签名")
    private String introduceSign;
    @ApiModelProperty(value = "收货地址")
    private String address;

    /**
     * 多的字段
     * */
    private Integer shopCartItemCount;
}
