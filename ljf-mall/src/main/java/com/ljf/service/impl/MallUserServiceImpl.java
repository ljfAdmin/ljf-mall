package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallCoupon;
import com.ljf.entity.MallUser;
import com.ljf.entity.MallUserCouponRecord;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.mapper.MallUserMapper;
import com.ljf.service.MallCouponService;
import com.ljf.service.MallUserCouponRecordService;
import com.ljf.service.MallUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ljf.utils.MD5Util;
import com.ljf.utils.MallUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallUserServiceImpl extends ServiceImpl<MallUserMapper, MallUser> implements MallUserService {

    @Autowired
    private MallCouponService mallCouponService;

    @Autowired
    private MallUserCouponRecordService mallUserCouponRecordService;

    @Override
    @Transactional // 这里开启事务
    public boolean changeUsersLockedFlag(Integer[] ids, int lockStatus) {
        if(ids == null || ids.length < 1){
            return false;
        }

        List<MallUser> mallUsers = baseMapper.selectBatchIds(Arrays.asList(ids));
        if(!CollectionUtils.isEmpty(mallUsers)){
            for (MallUser mallUser : mallUsers) {
                mallUser.setLockedFlag(lockStatus);
                int updated = baseMapper.updateById(mallUser);
                if(updated < 1){
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 前台用户登录
     * */
    @Override
    public String login(String loginName, String encrypt, HttpSession httpSession) {
        QueryWrapper<MallUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",loginName);
        queryWrapper.eq("password_md5",encrypt);
        MallUser mallUser = baseMapper.selectOne(queryWrapper);
        if(mallUser != null && httpSession != null){
            // @ApiModelProperty(value = "锁定标识字段(0-未锁定 1-已锁定)")
            if(mallUser.getLockedFlag() == 1){
                return ToFrontMessageConstantEnum.LOGIN_USER_LOCKED.getResult();
            }

            // 昵称太长，影响页面显示，昵称长度可以交给前端处理
            if(mallUser.getNickName() != null && mallUser.getNickName().length() > 7){
                String tempNickName = mallUser.getNickName().substring(0, 7) + "..";
                mallUser.setNickName(tempNickName);
            }

            // 对于前台，要封装成MallUserVO来处理
            MallUserVO mallUserVO = new MallUserVO();
            BeanUtils.copyProperties(mallUser,mallUserVO);
            // 设置购物车中的数量....

            httpSession.setAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY,mallUserVO);
            return ToFrontMessageConstantEnum.SUCCESS.getResult();
        }
        return ToFrontMessageConstantEnum.LOGIN_ERROR.getResult();
    }

    /**
     * 注册
     * */
    @Override
    public String register(String loginName, String password) {
        QueryWrapper<MallUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",loginName);
        MallUser user = baseMapper.selectOne(queryWrapper);
        if(user != null){
            return ToFrontMessageConstantEnum.SAME_LOGIN_NAME_EXIST.getResult();
        }

        // 设置注册用户对象
        MallUser registerUser = new MallUser();
        registerUser.setLoginName(loginName);
        registerUser.setNickName(loginName);
        registerUser.setPasswordMd5(MD5Util.encrypt(password));

        // 保存到数据库
        int inserted = baseMapper.insert(registerUser);
        if(inserted < 1){
            return ToFrontMessageConstantEnum.SAVE_FAILED.getResult();
        }

        // 添加注册赠送的优惠券
        List<MallCoupon> mallCoupons = mallCouponService.selectAvailableGiveCoupon();
        if(!CollectionUtils.isEmpty(mallCoupons)){
            for (MallCoupon mallCoupon : mallCoupons) {
                MallUserCouponRecord userCouponRecord = new MallUserCouponRecord();
                userCouponRecord.setUserId(registerUser.getUserId());
                userCouponRecord.setCouponId(mallCoupon.getCouponId());
                boolean saved = mallUserCouponRecordService.save(userCouponRecord);
                if(!saved){
                    return ToFrontMessageConstantEnum.SAVE_FAILED.getResult();
                }
            }
        }
        return ToFrontMessageConstantEnum.SUCCESS.getResult();
    }

    /**
     * 更新用户信息
     * */
    @Override
    public MallUserVO updateUserInfo(MallUser mallUser, HttpSession httpSession) {
        MallUserVO userTemp = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);

        MallUser userFromDB = baseMapper.selectById(userTemp.getUserId());

        if (userFromDB != null) {
            if (!StringUtils.isEmpty(mallUser.getNickName())) {
                userFromDB.setNickName(MallUtil.cleanString(mallUser.getNickName()));
            }
            if (!StringUtils.isEmpty(mallUser.getAddress())) {
                userFromDB.setAddress(MallUtil.cleanString(mallUser.getAddress()));
            }
            if (!StringUtils.isEmpty(mallUser.getIntroduceSign())) {
                userFromDB.setIntroduceSign(MallUtil.cleanString(mallUser.getIntroduceSign()));
            }

            if(baseMapper.updateById(userFromDB) > 0){
                MallUserVO mallUserVO = new MallUserVO();
                userFromDB = baseMapper.selectById(mallUser.getUserId());
                BeanUtils.copyProperties(userFromDB,mallUserVO);
                httpSession.setAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY,mallUserVO);
                return mallUserVO;
            }
        }
        return null;
    }
}
