package com.ljf.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.MallGoodsCategoryLevel;
import com.ljf.constant.MallGoodsSellStatusConstant;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallCoupon;
import com.ljf.entity.MallGoodsCategory;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.mapper.MallGoodsInfoMapper;
import com.ljf.service.MallGoodsCategoryService;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 */
@Controller
@RequestMapping("/admin")
public class MallGoodsInfoController {
    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Autowired
    private MallGoodsCategoryService mallGoodsCategoryService;

    @GetMapping("/goods")
    public String goodsPage(HttpServletRequest request) {
        request.setAttribute("path", "ljf_mall_goods");
        return "admin/mall_goods";
    }

    /**
     * 获取商品相关的分类信息
     *
     * 要修改一个商品的信息，但是商品的一些信息需要我们提前准备好，方便做回显
     *  比如，这里的商品分类信息，我们需要提前拿到，以及商品本身信息
     *  并跳转到指定的修改页面
     *
     *  但是这里并没有携带商品本身信息过去
     *
     * TODO: 这些分类信息当然也可以作为全局字典存储在全局作用域中
     * */
    @GetMapping("/goods/edit")
    public String edit(HttpServletRequest request) throws Exception {
        request.setAttribute("path", "edit");

        List<MallGoodsCategory> allGoodsCategories = mallGoodsCategoryService.list(null);

        // 查询所有的一级分类，一级分类特点：所有的parentId都为0
        // List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel());
        List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel(),allGoodsCategories);

        if(!CollectionUtils.isEmpty(firstLevelCategories)){
            // 查询一级分类下第一个实体下的所有二级分类
            //List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(firstLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
            List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(firstLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel(),allGoodsCategories);
            if(!CollectionUtils.isEmpty(secondLevelCategories)){
                // 查询二级分类下第一个实体下的所有三级分类
                //List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(secondLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(secondLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel(),allGoodsCategories);
                request.setAttribute("firstLevelCategories", firstLevelCategories);
                request.setAttribute("secondLevelCategories", secondLevelCategories);
                request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                request.setAttribute("path", "goods-edit");
                return "admin/mall_goods_edit";
            }
        }

        // MallException.fail("分类数据不完善");
        throw new Exception("分类数据不完善");
    }

    /**
     * 根据商品ID跳转到商品详细信息页面，并取得回显数据进行回显
     *
     *    注：这里的逻辑：这里不仅仅要查出所有的分类信息，还要将属于该商品的分类信息查询出来后续做高亮显示
     * */
    @GetMapping("/goods/edit/{goodsId}")
    public String editGoodsInfoById(HttpServletRequest request,
                                    @PathVariable("goodsId") Long goodsId) {
        request.setAttribute("path", "edit");
        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);

        List<MallGoodsCategory> allGoodsCategories = mallGoodsCategoryService.list(null);

        // 已经取得商品详情数据，额外处理分类的信息
        // 存在分类ID而且说明当前分类不是顶头分类
        if(goodsInfo.getGoodsCategoryId() != null && goodsInfo.getGoodsCategoryId() > 0){
            //有分类字段则查询相关分类数据返回给前端以供分类的三级联动显示
            MallGoodsCategory currentGoodsCategory = mallGoodsCategoryService.getById(goodsInfo.getGoodsCategoryId());
            //商品表中存储的分类id字段为三级分类的id，不为三级分类则是错误数据
            if(currentGoodsCategory != null && currentGoodsCategory.getCategoryLevel().equals(MallGoodsCategoryLevel.LEVEL_THREE.getLevel())){
                // 查询该三级分类的上面的二级分类和一级分类
                // 首先查询所有的一级分类
                //List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel());
                List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel(),allGoodsCategories);
                // 查询所有的二级分类

                // 查询所有的三级分类
                //List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(currentGoodsCategory.getParentId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(currentGoodsCategory.getParentId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel(),allGoodsCategories);

                // 查询当前三级分类的父类二级分类
                MallGoodsCategory currentSecondCategory = mallGoodsCategoryService.getById(currentGoodsCategory.getParentId());

                if(currentSecondCategory != null){
                    // 查询所有的二级分类
                    //List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(currentGoodsCategory.getParentId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
                    List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(currentGoodsCategory.getParentId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel(),allGoodsCategories);
                    // 根据当前二级分类查询出商品所属的一级分类
                    MallGoodsCategory currentFirstCategory = mallGoodsCategoryService.getById(currentSecondCategory.getParentId());
                    if(currentFirstCategory != null){
                        //所有分类数据都得到之后放到request对象中供前端读取
                        request.setAttribute("firstLevelCategories", firstLevelCategories);
                        request.setAttribute("secondLevelCategories", secondLevelCategories);
                        request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                        request.setAttribute("firstLevelCategoryId", currentFirstCategory.getCategoryId());
                        request.setAttribute("secondLevelCategoryId", currentSecondCategory.getCategoryId());
                        request.setAttribute("thirdLevelCategoryId", currentGoodsCategory.getCategoryId());
                    }
                }
            }
        }

        // 说明该商品还没有分类信息
        if(goodsInfo.getGoodsCategoryId() == 0L){
            // 查询所有的一级分类
            // List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel());
            List<MallGoodsCategory> firstLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(0L, MallGoodsCategoryLevel.LEVEL_ONE.getLevel(),allGoodsCategories);
            if(!CollectionUtils.isEmpty(firstLevelCategories)){
                // 查询一级分类列表中第一个实体的所有二级分类
                //List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(firstLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel());
                List<MallGoodsCategory> secondLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(firstLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_TWO.getLevel(),allGoodsCategories);
                if(!CollectionUtils.isEmpty(secondLevelCategories)){
                    // 查询二级分类列表中第二个实体的所有一级分类
                    //List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategories(secondLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel());
                    List<MallGoodsCategory> thirdLevelCategories = mallGoodsCategoryService.getAppointedLevelGoodsCategoriesFromAll(secondLevelCategories.get(0).getCategoryId(), MallGoodsCategoryLevel.LEVEL_THREE.getLevel(),allGoodsCategories);
                    request.setAttribute("firstLevelCategories", firstLevelCategories);
                    request.setAttribute("secondLevelCategories", secondLevelCategories);
                    request.setAttribute("thirdLevelCategories", thirdLevelCategories);
                }
            }
        }

        request.setAttribute("goods", goodsInfo);
        request.setAttribute("path", "goods-edit");
        return "admin/mall_goods_edit";
    }

    /**
     * 分页+条件显示商品信息
     *
     * 注：这里跟以往不同，这里添加上了条件的封装，除了page,limit,order等分页请求数据之外
     * */
    @GetMapping(value = "/goods/list")
    @ResponseBody
    public Result getGoodsInfoListPage(@RequestParam Map<String,Object> params) throws ParseException {
        if(StringUtils.isEmpty((CharSequence) params.get("page"))
                || StringUtils.isEmpty((CharSequence) params.get("limit"))){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        Integer currentPage = Integer.valueOf((String) params.get("page"));
        Integer limit = Integer.valueOf((String) params.get("limit"));
        Page<MallGoodsInfo> page = new Page<>(currentPage,limit);

        QueryWrapper<MallGoodsInfo> queryWrapper = new QueryWrapper<>();
        // 注：查看源码，可知下面方法等同于 LIKE '%值%'
        String goodsName = (String) params.get("goodsName");
        if(!StringUtils.isEmpty(goodsName)){
            queryWrapper.like("goods_name",goodsName);
        }

        if(!StringUtils.isEmpty(((String) params.get("goodsSellStatus")))){
            Integer goodsSellStatus = Integer.valueOf(((String) params.get("goodsSellStatus")));
            if(!Objects.isNull(goodsSellStatus)){
                queryWrapper.like("goods_sell_status",goodsSellStatus);
            }
        }

        if(!StringUtils.isEmpty(((String) params.get("startTime")))){
            Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(((String) params.get("startTime")));
            if(!Objects.isNull(startTime)){
                queryWrapper.gt("create_time",startTime);
            }
        }

        if(!StringUtils.isEmpty(((String) params.get("endTime")))){
            Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(((String) params.get("endTime")));
            if(!Objects.isNull(endTime)){
                queryWrapper.gt("create_time",endTime);
            }
        }

        queryWrapper.orderByDesc("goods_id");

        mallGoodsInfoService.page(page,queryWrapper);
        return ResultGenerator.genSuccessResult(page);
    }

    /**
     * 添加一个商品
     * */
    @PostMapping(value = "/goods/save")
    @ResponseBody
    public Result saveGoodsInfo(@RequestBody MallGoodsInfo goodsInfo){
        // 注：当判定参数较少时，没有必要调用judgeGoodsInfoRelevantParamsLegal()方法
        //      而且短路与的存在未必效率低！！！！！！！
        if(!this.judgeGoodsInfoRelevantParamsLegal(goodsInfo.getGoodsName(),goodsInfo.getGoodsIntro(),goodsInfo.getTag()
                ,goodsInfo.getOriginalPrice(),goodsInfo.getGoodsCategoryId(),goodsInfo.getSellingPrice()
                ,goodsInfo.getStockNum(),goodsInfo.getGoodsSellStatus(),goodsInfo.getGoodsCoverImg()
                ,goodsInfo.getGoodsDetailContent())){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean saved = mallGoodsInfoService.save(goodsInfo);
        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 修改一个商品信息
     * */
    @PostMapping(value = "/goods/update")
    @ResponseBody
    public Result updateGoodsInfo(@RequestBody MallGoodsInfo goodsInfo){
        // 注：当判定参数较少时，没有必要调用judgeGoodsInfoRelevantParamsLegal()方法
        //      而且短路与的存在未必比时间复杂度O(n)的效率低！！！！！！！
        if(!this.judgeGoodsInfoRelevantParamsLegal(goodsInfo.getGoodsId(),goodsInfo.getGoodsName(),goodsInfo.getGoodsIntro(),goodsInfo.getTag()
                ,goodsInfo.getOriginalPrice(),goodsInfo.getGoodsCategoryId(),goodsInfo.getSellingPrice()
                ,goodsInfo.getStockNum(),goodsInfo.getGoodsSellStatus(),goodsInfo.getGoodsCoverImg()
                ,goodsInfo.getGoodsDetailContent())){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        boolean updated = mallGoodsInfoService.updateById(goodsInfo);
        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 根据商品ID，查询商品详情
     * */
    @GetMapping(value = "/goods/info/{id}")
    @ResponseBody
    public Result getGoodsInfoById(@PathVariable("id") Long goodsId){
        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
        return goodsInfo == null ? ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SELECT_DETAIL_FAILED.getResult()) : ResultGenerator.genSuccessResult(goodsInfo);
    }

    /**
     * 批量修改销售状态
     * */
    @PutMapping(value = "/goods/status/{sellStatus}")
    @ResponseBody
    public Result delete(@RequestBody Long[] ids, @PathVariable("sellStatus") Integer goodsSellStatus){
        // 注：当判定参数较少时，没有必要调用judgeGoodsInfoRelevantParamsLegal()方法
        //      而且短路与的存在未必比时间复杂度O(n)的效率低！！！！！！！
        if(ids == null || ids.length < 1){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.PLEASE_INPUT_REQUIRED_PARAM.getResult());
        }

        // 注：这里也需要判别状态是否合理，至于这里的状态判别不在方法的考虑范围之内，
        //      可以针对每种实体类创建对应的工具类
        if(!MallGoodsSellStatusConstant.judgeStatusIsLegal(goodsSellStatus)){
            return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.GOODS_INFO_SELL_STATUS_EXCEPTION.getResult());
        }

        for (Long id : ids) {
            MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(id);
            UpdateWrapper<MallGoodsInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("goods_sell_status",goodsSellStatus);
            boolean updated = mallGoodsInfoService.update(goodsInfo, updateWrapper);
            if(!updated){
                return ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
            }
        }

        return ResultGenerator.genSuccessResult();
    }

    /**
     * 根据传入的类型判断传入是否符合要求
     *   return:
     *      true：表示传入参数符合
     *      false：表示传入参数不符合
     * 规则：
     *  如果是传入的是字符串，则判断其是否为Null以及其是否长度小于1
     *  如果是Long、Byte、Integer等，则直接判断其是否为空
     *  至于其他细化的判断，比如某个参数是否在指定的值之间，则不考虑
     *
     * TODO:是否可以优化？
     */
    private boolean judgeGoodsInfoRelevantParamsLegal(Object... objects){
        // 注：instanceof在实际开发中的作用是：在进行类型转换之前，判断一个对象是否是一个类型的实例，
        //      如果是进行类型转换，如果不是就不进行类型转换。这样判断程序会更健壮，
        //      减少 ClassCastException异常的抛出
        boolean ans = true;
        for (int i = 0;i < objects.length;i ++){
            if(objects[i] instanceof String){
                if(StringUtils.isEmpty((CharSequence) objects[i])){
                    ans = false;
                    break;
                }
            }else if(objects[i] instanceof Byte){
                if(Objects.isNull(objects[i])){
                    ans = false;
                    break;
                }
            }else if(objects[i] instanceof Long){
                if(Objects.isNull(objects[i])){
                    ans = false;
                    break;
                }
            }else if(objects[i] instanceof Integer){
                if(Objects.isNull(objects[i])){
                    ans = false;
                    break;
                }
            }else if(objects[i] instanceof Object[]){
                Object[] temp = ((Object[]) objects[i]);
                if(temp == null || temp.length < 1){
                    ans = false;
                    break;
                }
            }
        }

        return ans;
    }


    /*public static void main(String[] args) {
        MallGoodsInfoController controller = new MallGoodsInfoController();
        String[] strings = new String[10];
        boolean b = controller.judgeGoodsInfoRelevantParamsLegal(strings);
        System.out.println(b);//true
    }*/
}

