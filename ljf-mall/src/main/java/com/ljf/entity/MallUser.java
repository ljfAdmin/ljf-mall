package com.ljf.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_user")
@ApiModel(value="MallUser对象", description="用户")
public class MallUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户主键id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "登陆名称(默认为手机号)")
    private String loginName;

    @ApiModelProperty(value = "MD5加密后的密码")
    private String passwordMd5;

    @ApiModelProperty(value = "个性签名")
    private String introduceSign;

    @ApiModelProperty(value = "收货地址")
    private String address;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "注销标识字段(0-正常 1-已注销)")
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "锁定标识字段(0-未锁定 1-已锁定)")
    private Integer lockedFlag;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "注册时间")
    private Date createTime;


}
