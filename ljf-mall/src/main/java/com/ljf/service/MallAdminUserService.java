package com.ljf.service;

import com.ljf.entity.MallAdminUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallAdminUserService extends IService<MallAdminUser> {
    /**
     * 管理员登录
     * */
    public MallAdminUser login(String userName,String password);

    /**
     * 获取管理员ID获取管理员信息
     * */
    public MallAdminUser getAdminUserById(Integer adminUserId);

    /**
     * 修改当前管理员登录用户的密码
     * */
    public boolean updateAdminUserPassword(Integer adminUserId,String originalPassword,String newPassword);

    /**
     * 修改管理员的名称信息
     * */
    public boolean updateAdminUserName(Integer loginUserId, String loginUserName, String nickName);

}
