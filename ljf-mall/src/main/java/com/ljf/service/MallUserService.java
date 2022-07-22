package com.ljf.service;

import com.ljf.entity.MallUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ljf.entity.vo.MallUserVO;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
public interface MallUserService extends IService<MallUser> {

    boolean changeUsersLockedFlag(Integer[] ids, int lockStatus);

    String login(String loginName, String encrypt, HttpSession httpSession);

    String register(String loginName, String password);

    MallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession);
}
