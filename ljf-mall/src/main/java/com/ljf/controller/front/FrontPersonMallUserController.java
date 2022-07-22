package com.ljf.controller.front;

import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallUser;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.service.MallCouponService;
import com.ljf.service.MallUserService;
import com.ljf.utils.HttpUtil;
import com.ljf.utils.MD5Util;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class FrontPersonMallUserController {
    @Autowired
    private MallUserService mallUserService;

    /**
     * 跳转到个人页面
     * */
    @GetMapping("/personal")
    public String personalPage(HttpServletRequest request,
                               HttpSession httpSession) {
        request.setAttribute("path", "personal");
        return "mall/personal";
    }

    /**
     * 退出登录
     * */
    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        return "mall/login";
    }

    /**
     *  去登录压面
     * */
    @GetMapping({"/login", "login.html"})
    public String loginPage(HttpServletRequest request) throws Exception {
        if (HttpUtil.isAjaxRequest(request)) {
            throw new Exception("请先登录！");
        }
        return "mall/login";
    }

    /**
     * 去注册页面
     * */
    @GetMapping({"/register", "register.html"})
    public String registerPage() {
        return "mall/register";
    }

    /**
     * 去地址页面
     * */
    @GetMapping("/personal/addresses")
    public String addressesPage() {
        return "mall/addresses";
    }


    /**
     * 登录提交表单
     * */
    @PostMapping("/login")
    @ResponseBody
    public Result login(@RequestParam("loginName") String loginName,
                        @RequestParam("verifyCode") String verifyCode,
                        @RequestParam("password") String password,
                        HttpSession httpSession) {
        if (StringUtils.isEmpty(loginName)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_NAME_NULL.getResult());
        }
        if (StringUtils.isEmpty(password)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (StringUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }

        String kaptchaCode = (String) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY);

        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.toLowerCase().equals(kaptchaCode)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }

        httpSession.setAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY, null);

        String loginResult = mallUserService.login(loginName, MD5Util.encrypt(password), httpSession);
        //登录成功
        if (ToFrontMessageConstantEnum.SUCCESS.getResult().equals(loginResult)) {
            //删除session中的verifyCode
            httpSession.removeAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY);
            return ResultGenerator.genSuccessResult();
        }

        //登录失败
        return ResultGenerator.genFailResult(loginResult);
    }

    @PostMapping("/register")
    @ResponseBody
    public Result register(@RequestParam("loginName") String loginName,
                           @RequestParam("verifyCode") String verifyCode,
                           @RequestParam("password") String password,
                           HttpSession httpSession) {
        if (StringUtils.isEmpty(loginName)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_NAME_NULL.getResult());
        }
        if (StringUtils.isEmpty(password)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_PASSWORD_NULL.getResult());
        }
        if (StringUtils.isEmpty(verifyCode)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_VERIFY_CODE_NULL.getResult());
        }

        String kaptchaCode = (String) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY);
        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.toLowerCase().equals(kaptchaCode)) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.LOGIN_VERIFY_CODE_ERROR.getResult());
        }
        httpSession.setAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY, null);
        String registerResult = mallUserService.register(loginName, password);

        //注册成功
        if (ToFrontMessageConstantEnum.SUCCESS.getResult().equals(registerResult)) {
            //删除session中的verifyCode
            httpSession.removeAttribute(FrontMallUserInfoConstant.MALL_VERIFY_CODE_KEY);
            return ResultGenerator.genSuccessResult();
        }
        //注册失败
        return ResultGenerator.genFailResult(registerResult);
    }

    /**
     * 用户更新自己信息
     * */
    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public Result updateInfo(@RequestBody MallUser mallUser, HttpSession httpSession) {
        MallUserVO mallUserTemp = mallUserService.updateUserInfo(mallUser, httpSession);
        if (mallUserTemp == null) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
        } else {
            //返回成功
            return ResultGenerator.genSuccessResult();
        }
    }

}
