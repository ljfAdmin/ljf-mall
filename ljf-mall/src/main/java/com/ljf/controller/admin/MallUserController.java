package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.enums.ToFrontMessageConstantEnum;
import com.ljf.entity.MallUser;
import com.ljf.service.MallUserService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Api(value = "后台普通用户控制层类")
@RestController
@RequestMapping("/admin")
public class MallUserController {
    @Autowired
    private MallUserService mallUserService;

    @ApiOperation(value = "跳转到用户信息页面")
    @GetMapping("/users")
    public String toUsersPage(HttpServletRequest request) {
        request.setAttribute("path", "users");
        return "admin/mall_user";
    }

    /**
     * 分页查询列表
     * */
    @ApiOperation(value = "分页查询用户信息列表")
    @GetMapping(value = "/users/list")
    @ResponseBody
    public Result getUsersListPageWhere(@RequestParam Map<String, Object> params) {
        if (StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf((String) params.get("page"));
        Integer limit = Integer.valueOf((String) params.get("limit"));
        Page<MallUser> page = new Page<>(currentPage,limit);

        QueryWrapper<MallUser> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty((String) params.get("loginName"))){
            queryWrapper.eq("login_name",(String) params.get("loginName"));

        }
        queryWrapper.orderByDesc("create_time");

        mallUserService.page(page,queryWrapper);
        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     */
    @ApiOperation(value = "用户禁用与解除禁用(0-未锁定 1-已锁定)")
    @PostMapping(value = "/users/lock/{lockStatus}")
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids, @PathVariable("lockStatus") int lockStatus) {
        if (ids == null || ids.length < 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        // @ApiModelProperty(value = "锁定标识字段(0-未锁定 1-已锁定)")
        // private Integer lockedFlag;
        if (lockStatus != 0 && lockStatus != 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.INPUT_PARAM_EXCEPTION.getResult());
        }

        return mallUserService.changeUsersLockedFlag(ids,lockStatus) ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.MALL_USER_LOCKED_FLAG_UPDATED_FAILURE.getResult());

    }

}

