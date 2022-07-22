package com.ljf.entity;

import com.baomidou.mybatisplus.annotation.*;

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
@TableName("mall_admin_user")
@ApiModel(value="MallAdminUser对象", description="管理员")
public class MallAdminUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "管理员id")
    @TableId(value = "admin_user_id", type = IdType.AUTO)
    private Integer adminUserId;

    @ApiModelProperty(value = "管理员登陆名称")
    private String loginUserName;

    @ApiModelProperty(value = "管理员登陆密码，加密过后")
    private String loginPassword;

    @ApiModelProperty(value = "管理员显示昵称")
    private String nickName;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty(value = "是否锁定 0未锁定 1已锁定无法登陆")
    private Integer locked;


}
