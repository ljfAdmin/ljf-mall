package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.MallGoodsCategoryLevel;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallGoodsCategory;
import com.ljf.service.MallGoodsCategoryService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 *
 * 商品分类相关操作
 */
@Controller
@RequestMapping("/admin")
public class MallGoodsCategoryController {
    @Autowired
    private MallGoodsCategoryService mallGoodsCategoryService;

    /**
     * 进入分类页面，并携带一些参数
     * categoryLevel:分类级别；
     * parentId:父分类id
     * backParentId:父分类id的排序值(字段越大越靠前)
     * */
    @GetMapping("/categories")
    public String toCategoriesPage(HttpServletRequest request,
                                 @RequestParam("categoryLevel") Integer categoryLevel,
                                 @RequestParam("parentId") Long parentId,
                                 @RequestParam("backParentId") Integer backParentId) throws Exception {
        // 这个判别在前端处理更好
        if (categoryLevel == null || categoryLevel < 1 || categoryLevel > 3) {
            throw new Exception("参数异常");
        }

        request.setAttribute("path", "ljf_mall_category");
        request.setAttribute("parentId", parentId);
        request.setAttribute("backParentId", backParentId);
        request.setAttribute("categoryLevel", categoryLevel);
        return "admin/mall_category";
    }

    /**
     * 分页列表查询
     * */
    @GetMapping(value = "/categories/list")
    @ResponseBody
    public Result getGoodCategoryListPage(@RequestParam Map<String, Object> params) {
        // 这里的categoryLevel的处理同上(这里省略)，但是这个判别在前端处理更好
        if (params == null || StringUtils.isEmpty((CharSequence) params.get("page"))
                || StringUtils.isEmpty((CharSequence) params.get("limit"))
                || StringUtils.isEmpty((CharSequence) params.get("categoryLevel"))
                || StringUtils.isEmpty((CharSequence) params.get("parentId"))) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf(((String) params.get("page")));
        Integer limit = Integer.valueOf(((String) params.get("limit")));
        Page<MallGoodsCategory> page = new Page<>(currentPage,limit);

        QueryWrapper<MallGoodsCategory> queryWrapper = new QueryWrapper<>();
        Integer categoryLevel = Integer.valueOf(((String) params.get("categoryLevel")));
        Long parentId = Long.valueOf(((String) params.get("parentId")));
        // Integer start = (currentPage - 1) * limit;

        queryWrapper.eq("category_level",categoryLevel);
        queryWrapper.eq("parent_id",parentId);
        queryWrapper.orderByDesc("category_rank");
        // queryWrapper.last("limit "+start+","+limit);
        mallGoodsCategoryService.page(page,queryWrapper);

        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 查询一个父级分类下的子分类
     *  注：下面的方法中有很多重复代码，可以抽取为方法
     *
     *  进行改进：
     * */
    @GetMapping(value = "/categories/listForSelect")
    @ResponseBody
    public Result listCategoryUnderParentForSelect(@RequestParam("categoryId") Long categoryId){
        if(categoryId == null || categoryId.longValue() < 1L){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        List<MallGoodsCategory> allGoodsCategories = mallGoodsCategoryService.list(null);

        MallGoodsCategory goodsCategory = mallGoodsCategoryService.getById(categoryId);

        // 同样的，这里可以通过前端进行判断
        if(goodsCategory == null || MallGoodsCategoryLevel.LEVEL_THREE.getLevel().equals(goodsCategory.getCategoryLevel())){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.INPUT_PARAM_EXCEPTION.getResult());
        }

        Map<String ,Object> categoryMapResult = new HashMap<>(4);
        // 如果是一级分类则返回当前一级分类下的所有二级分类，以及二级分类列表中第一条数据下的所有三级分类列表
        if(MallGoodsCategoryLevel.LEVEL_ONE.getLevel().equals(goodsCategory.getCategoryLevel())){
            // 首先根据一级分类的ID查询出所有二级分类
            /*QueryWrapper<MallGoodsCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id",categoryId);
            queryWrapper.eq("category_level",MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
            queryWrapper.orderByDesc("category_rank");
            List<MallGoodsCategory> secondGoodsCategories = mallGoodsCategoryService.list(queryWrapper);*/

            //List<MallGoodsCategory> secondGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(categoryId, MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
            List<MallGoodsCategory> secondGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(categoryId, MallGoodsCategoryLevel.LEVEL_TWO.getLevel(),allGoodsCategories);

            // 然后再查询出来二级分类中第一个二级分类的所有三级分类
            if(!CollectionUtils.isEmpty(secondGoodsCategories)){// 会判断是否为null，以及是否为空
                /*MallGoodsCategory firstSecondGoodsCategory = secondGoodsCategories.get(0);
                QueryWrapper<MallGoodsCategory> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("parent_id",firstSecondGoodsCategory.getCategoryId());
                queryWrapper1.eq("category_level",MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                queryWrapper1.orderByDesc("category_rank");
                List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.list(queryWrapper1);*/

                //List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(
                //        secondGoodsCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(
                        secondGoodsCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel(),allGoodsCategories);

                categoryMapResult.put("secondLevelCategories",secondGoodsCategories);
                categoryMapResult.put("thirdLevelCategories",threeGoodsCategories);
            }
        }

        // 如果是二级分类，则返回当前分类下的所有三级分类列表
        if(MallGoodsCategoryLevel.LEVEL_TWO.getLevel().equals(goodsCategory.getCategoryLevel())){
            /*QueryWrapper<MallGoodsCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id",categoryId);
            queryWrapper.eq("category_level",MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
            queryWrapper.orderByDesc("category_rank");
            List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.list(queryWrapper);*/

            //List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(categoryId, MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
            List<MallGoodsCategory> threeGoodsCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(categoryId, MallGoodsCategoryLevel.LEVEL_THREE.getLevel(),allGoodsCategories);
            categoryMapResult.put("thirdLevelCategories",threeGoodsCategories);
        }


        return ResultGenerator.genSuccessResult(categoryMapResult);
    }

    /**
     * 添加
     * */
    @PostMapping(value = "/categories/save")
    @ResponseBody
    public Result saveGoodsCategory(@RequestBody MallGoodsCategory goodsCategory) {
        // 首先又是参数判定，可以放在前端做
        if(goodsCategory == null || Objects.isNull(goodsCategory.getCategoryLevel())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())
                || Objects.isNull(goodsCategory.getParentId())
                || Objects.isNull(goodsCategory.getCategoryRank())){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean saved = mallGoodsCategoryService.save(goodsCategory);

        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 修改，注：这里的参数判定除了放在前端外，后端也可以对每种实体类的参数判别进行抽取方法
     * */
    @PostMapping(value = "/categories/update")
    @ResponseBody
    public Result updateGoodsCategory(@RequestBody MallGoodsCategory goodsCategory) {
        if (goodsCategory == null || Objects.isNull(goodsCategory.getCategoryId())
                || Objects.isNull(goodsCategory.getCategoryLevel())
                || StringUtils.isEmpty(goodsCategory.getCategoryName())
                || Objects.isNull(goodsCategory.getParentId())
                || Objects.isNull(goodsCategory.getCategoryRank())) {
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean updated = mallGoodsCategoryService.updateById(goodsCategory);
        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 获得商品分类的详细信息
     * */
    @GetMapping(value = "/categories/info/{id}")
    @ResponseBody
    public Result getGoodsCategoryInfo(@PathVariable("id") Long id) {
        MallGoodsCategory goodsCategory = mallGoodsCategoryService.getById(id);
        return goodsCategory == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(goodsCategory);
    }

    /**
     * 分类的删除
     * */
    @PostMapping(value = "/categories/delete")
    @ResponseBody
    //public Result deleteGoodsCategoryByIds(@RequestBody Integer[] ids) {
    public Result deleteGoodsCategoryByIds(@RequestBody Long[] ids) {
        if(ids == null || ids.length <= 0){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean deleted = mallGoodsCategoryService.removeByIds(Arrays.asList(ids));
        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

}

