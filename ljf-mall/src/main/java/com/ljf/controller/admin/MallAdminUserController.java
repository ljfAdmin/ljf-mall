package com.ljf.controller.admin;


import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallAdminUser;
import com.ljf.service.MallAdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Api(value = "后台管理员用户控制层类")
@Controller
@RequestMapping("/admin")
public class MallAdminUserController {
    @Autowired
    private MallAdminUserService mallAdminUserService;

    /**
     * 跳转到登录页面
     * */
    @ApiOperation(value = "跳转到登录页面")
    @GetMapping(value = "/login")
    public String login(){
        return "admin/login";
    }

    /**
     * 跳转到管理员后台首页
     * */
    @ApiOperation(value = "跳转到管理员后台首页")
    @GetMapping(value = {"","/","index","index.html"})
    public String index(HttpServletRequest request){
        request.setAttribute("path","index");
        return "admin/index";
    }

    /**
     * 提交登录表单
     *
     *  到某个页面或者地址
     * */
    @ApiOperation(value = "后台管理员登录表单提交")
    @PostMapping(value = "/login")
    public String login(@RequestParam("userName") String userName,
                        @RequestParam("password") String password,
                        @RequestParam("verifyCode") String verifyCode,
                        HttpSession session){
        if(StringUtils.isEmpty(verifyCode)){
            session.setAttribute("errorMsg","验证码不能为空");
            return "admin/login";
        }

        if(StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)){
            session.setAttribute("errorMsg","用户或者密码不能为空");
            return "admin/login";
        }

        // 生成的验证码存入session域中，与我们的输入进行比对
        String kaptchaCode = session.getAttribute("verifyCode")+"";
        if(!StringUtils.equalsIgnoreCase(kaptchaCode,verifyCode)){
            session.setAttribute("errorMsg","验证码输入错误");
            return "admin/login";
        }

        // 根据我们输入的用户名和密码进行登录
        MallAdminUser adminUser = mallAdminUserService.login(userName, password);
        if(adminUser != null){
            // 存入对应的数据到session域中
            session.setAttribute("loginUser", adminUser.getNickName());
            session.setAttribute("loginUserId", adminUser.getAdminUserId());
            // 设置session过期时间，7200秒，即2小时
            session.setMaxInactiveInterval(60 * 60 * 2);
            // 注：这里注意视图解析器的前后缀失效
            return "redirect:/admin/index";
        }else{
            session.setAttribute("errorMsg","登录错误，该管理员不存在！");
            return "admin/login";
        }
    }

    /**
     * <a th:href="@{/admin/profile}"
     *    th:class="${path}=='profile'?'nav-link active':'nav-link'">
     *     <i class="fa fa-user-secret nav-icon"></i>
     *     <p>修改密码</p>
     * </a>
     *
     * 点击发送请求，跳转到修改管理员信息的admin/profile.html页面，并做数据回显
     * */
    @ApiOperation(value = "携带数据并跳转到修改管理员信息页面")
    @GetMapping("/profile")
    public String toAdminEditPage(HttpServletRequest request) {
        Integer loginUserId = (Integer) request.getSession().getAttribute("loginUserId");
        MallAdminUser adminUser = mallAdminUserService.getAdminUserById(loginUserId);
        if(adminUser == null){// 没有登录或者已经过期
            return "admin/login";
        }

        // 做数据回显
        request.setAttribute("path","profile");
        request.setAttribute("loginUserName",adminUser.getLoginUserName());
        request.setAttribute("nickName",adminUser.getNickName());
        return "admin/profile";
    }

    /**
     * 修改密码：发送的AJAX POST请求
     * */
    @ApiOperation(value = "修改管理员用户自己密码")
    @PostMapping("/profile/password")
    @ResponseBody
    public String updateAdminPassword(@RequestParam("originalPassword") String originalPassword,
                                      @RequestParam("newPassword") String newPassword,
                                      HttpServletRequest request){
        if(StringUtils.isEmpty(originalPassword) || StringUtils.isEmpty(newPassword)){
            return "两个输入不能为空";
        }

        Integer loginUserId = (Integer) request.getSession().getAttribute("loginUserId");
        if(mallAdminUserService.updateAdminUserPassword(loginUserId,originalPassword,newPassword)){
            // 修改成功之后，清除session中的数据，前端控制跳转至登录页面
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return ToFrontMessageConstantEnum.SUCCESS.getResult();
        }else{
            return "修改失败";
        }
    }

    /**
     * 修改个人信息，或者说修改名称等
     * */
    @ApiOperation(value = "修改管理员自己的昵称")
    @PostMapping(value = "/profile/name")
    @ResponseBody
    public String updateAdminUser(@RequestParam("loginUserName") String loginUserName,
                                  @RequestParam("nickName") String nickName,
                                  HttpServletRequest request){
        if(StringUtils.isEmpty(loginUserName) || StringUtils.isEmpty(nickName)){
            return "两个输入不能为空";
        }

        Integer loginUserId = (Integer) request.getSession().getAttribute("loginUserId");
        if(mallAdminUserService.updateAdminUserName(loginUserId,loginUserName,nickName)){
            return ToFrontMessageConstantEnum.SUCCESS.getResult();
        }else{
            return "修改失败";
        }
    }

    /**
     * 退出登录，一般退出登录之后会进入登录页面
     * */
    @ApiOperation(value = "退出登录")
    @GetMapping(value = "/logout")
    public String logout(HttpServletRequest request){
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("errorMsg");
        return "admin/login";
    }

}

