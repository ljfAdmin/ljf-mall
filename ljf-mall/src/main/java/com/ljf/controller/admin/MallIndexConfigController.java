package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.MallIndexConfigTypeEnum;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallIndexConfig;
import com.ljf.service.MallIndexConfigService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 *
 * 首页配置项表相关,除了主键之外，还有goods_id
 */
@Controller
@RequestMapping("/admin")
public class MallIndexConfigController {
    @Autowired
    private MallIndexConfigService mallIndexConfigService;

    /**
     * 根据类型 跳转到对应的页面
     *
     * 注：根据类型configType选择不同的展示商品内容
     *      3：热销商品
     *      4：新品上线
     *      5：为您推荐
     *  1-搜索框热搜 2-搜索下拉框热搜 3-(首页)热销商品 4-(首页)新品上线 5-(首页)为你推荐
     * */
    @GetMapping("/indexConfigs")
    public String toIndexConfigsPage(HttpServletRequest request,
                                     @RequestParam("configType") Integer configType) throws Exception {
        MallIndexConfigTypeEnum mallIndexConfigTypeEnumByType = MallIndexConfigTypeEnum.getMallIndexConfigTypeEnumByType(configType);
        if(MallIndexConfigTypeEnum.DEFAULT.equals(mallIndexConfigTypeEnumByType)){
            throw new Exception("参数异常");
        }

        request.setAttribute("path", mallIndexConfigTypeEnumByType.getName());
        request.setAttribute("configType", configType);
        return "admin/mall_index_config";
    }

    /**
     * 首先配置项分页显示，并根据config_type进行条件查询
     * */
    @GetMapping(value = "/indexConfigs/list")
    @ResponseBody
    public Result getIndexConfigListPage(@RequestParam Map<String, Object> params){
        if (StringUtils.isEmpty((CharSequence) params.get("page"))
                || StringUtils.isEmpty((CharSequence) params.get("limit"))) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf(((String) params.get("page")));
        Integer limit = Integer.valueOf(((String) params.get("limit")));
        Page<MallIndexConfig> page = new Page<>(currentPage,limit);

        QueryWrapper<MallIndexConfig> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(((String) params.get("configType")))){
            Integer configType = Integer.valueOf(((String) params.get("configType")));
            queryWrapper.eq("config_type",configType);
        }

        queryWrapper.orderByDesc("config_rank");

        mallIndexConfigService.page(page,queryWrapper);
        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 添加
     * */
    @PostMapping(value = "/indexConfigs/save")
    @ResponseBody
    public Result saveMallIndexConfig(@RequestBody MallIndexConfig indexConfig) {
        if (Objects.isNull(indexConfig.getConfigType())
                || StringUtils.isEmpty(indexConfig.getConfigName())
                || Objects.isNull(indexConfig.getConfigRank())) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }
        boolean saved = mallIndexConfigService.save(indexConfig);
        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 修改
     * */
    @PostMapping(value = "/indexConfigs/update")
    @ResponseBody
    public Result update(@RequestBody MallIndexConfig indexConfig) {
        if (Objects.isNull(indexConfig.getConfigType())
                || Objects.isNull(indexConfig.getConfigId())
                || StringUtils.isEmpty(indexConfig.getConfigName())
                || Objects.isNull(indexConfig.getConfigRank())) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }
        boolean updated = mallIndexConfigService.updateById(indexConfig);
        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 根据ID查看详情
     * */
    @GetMapping("/indexConfigs/info/{id}")
    @ResponseBody
    public Result info(@PathVariable("id") Long id) {
        MallIndexConfig indexConfig = mallIndexConfigService.getById(id);
        return indexConfig == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(indexConfig);
    }

    /**
     * 删除
     * */
    @PostMapping(value = "/indexConfigs/delete")
    @ResponseBody
    public Result delete(@RequestBody Long[] ids) {
        if (ids.length < 1) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }
        boolean deleted = mallIndexConfigService.removeByIds(Arrays.asList(ids));
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

}
