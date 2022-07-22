package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.entity.MallAdminUser;
import com.ljf.mapper.MallAdminUserMapper;
import com.ljf.service.MallAdminUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.utils.MD5Util;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallAdminUserServiceImpl extends ServiceImpl<MallAdminUserMapper, MallAdminUser> implements MallAdminUserService {

    /**
     * 登录方法，根据用户名和密码查询数据
     * */
    @Override
    public MallAdminUser login(String userName, String password) {
        QueryWrapper<MallAdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_user_name",userName);
        queryWrapper.eq("login_password", MD5Util.encrypt(password));
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 获取管理员ID获取管理员信息
     * */
    @Override
    public MallAdminUser getAdminUserById(Integer adminUserId) {
        return baseMapper.selectById(adminUserId);
    }

    /**
     * 修改当前管理员登录用户的密码
     * */
    @Override
    public boolean updateAdminUserPassword(Integer adminUserId, String originalPassword, String newPassword) {
        MallAdminUser adminUser = baseMapper.selectById(adminUserId);
        //当前用户非空才可以进行更改
        if(adminUser != null){
            String originalPasswordMD5 = MD5Util.encrypt(originalPassword);
            // 比较原密码是否正确
            if(originalPasswordMD5.equals(adminUser.getLoginPassword())){
                // 设置新密码并修改
                adminUser.setLoginPassword(MD5Util.encrypt(newPassword));
                if(baseMapper.updateById(adminUser) > 0){//修改成功
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 修改管理员的名称信息，包括登录名和昵称
     * */
    @Override
    public boolean updateAdminUserName(Integer loginUserId, String loginUserName, String nickName) {
        MallAdminUser adminUser = baseMapper.selectById(loginUserId);
        // 当前用户非空才可以进行更改
        if(adminUser != null){
            // 设置新名称并修改
            adminUser.setLoginUserName(loginUserName);
            adminUser.setNickName(nickName);
            if(baseMapper.updateById(adminUser) > 0){//修改成功
                return true;
            }
        }
        return false;
    }
}
