package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallCarousel;
import com.ljf.service.MallCarouselService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
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
 * 首页轮播图相关操作
 */
@Controller
@RequestMapping("/admin")
public class MallCarouselController {
    @Autowired
    private MallCarouselService mallCarouselService;

    /**
     * 转到轮播图页面
     */
    @GetMapping(value = "/carousels")
    public String toCarouselPage(HttpServletRequest request){
        request.setAttribute("path","ljf_mall_carousel");
        return "admin/mall_carousel";
    }

    /**
     * 分页查询，这里请求参数是三个，可以封装成一个Java对象或者一个Map或者一个一个写
     * */
    @RequestMapping(value = "/carousels/list", method = RequestMethod.GET)
    @ResponseBody
    public Result<Page<MallCarousel>> getCarouselListPage(@RequestParam Map<String,Object> params){
        if(StringUtils.isEmpty((CharSequence) params.get("page")) || StringUtils.isEmpty((CharSequence) params.get("limit"))){
            return ResultGenerator.genFailResult("请输入参数page和limit完全");
        }

        Integer currentPage = Integer.valueOf(((String) params.get("page")));
        Integer limit = Integer.valueOf(((String) params.get("limit")));
        // Integer start = (currentPage - 1) * limit;

        Page<MallCarousel> page = new Page<>(currentPage,limit);
        QueryWrapper<MallCarousel> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("carousel_rank");
        // queryWrapper.last("limit "+start+","+limit);
        mallCarouselService.page(page, queryWrapper);
        /* long total = page.getTotal();
         long current = page.getCurrent();
         List<MallCarousel> records = page.getRecords();
         long size = page.getSize();
         long pages = page.getPages();
        System.out.println(total);//2
        System.out.println(current);//1
        System.out.println(size);//10
        System.out.println(pages);//1*/
        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 添加操作
     * */
    @RequestMapping(value = "/carousels/save",method = RequestMethod.POST)
    @ResponseBody
    public Result saveCarousel(@RequestBody MallCarousel mallCarousel){
        if(mallCarousel == null || StringUtils.isEmpty(mallCarousel.getCarouselUrl()) ||
                Objects.isNull(mallCarousel.getCarouselRank())){
            // 至于mallCarousel.getRedirectUrl()默认是不跳转
            return ResultGenerator.genFailResult("请输入必要的参数");
        }

        boolean save = mallCarouselService.save(mallCarousel);//插入一条记录（选择字段，策略插入）
        // return save ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult("添加失败");
        return save ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 根据ID修改对应的数据
     * */
    @RequestMapping(value = "/carousels/update",method = RequestMethod.POST)
    @ResponseBody
    public Result updateCarousel(@RequestBody MallCarousel mallCarousel){
        if(mallCarousel == null || Objects.isNull(mallCarousel.getCarouselId())
                || StringUtils.isEmpty(mallCarousel.getCarouselUrl())
                || Objects.isNull(mallCarousel.getCarouselRank())){
            return ResultGenerator.genFailResult("请输入必要的参数");
        }

        boolean updated = mallCarouselService.updateById(mallCarousel);
        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 详情
     * */
    @RequestMapping(value = "/carousels/info/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Result getCarouselInfoById(@PathVariable("id") Integer carouselId){
        MallCarousel mallCarousel = mallCarouselService.getById(carouselId);
        return mallCarousel == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(mallCarousel);
    }

    /**
     * 删除操作
     * */
    @RequestMapping(value = "/carousels/delete",method = RequestMethod.POST)
    @ResponseBody
    public Result deleteCarousel(@RequestBody Integer[] ids){
        if(ids == null || ids.length <= 0){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean deleted = mallCarouselService.removeByIds(Arrays.asList(ids));
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

}

