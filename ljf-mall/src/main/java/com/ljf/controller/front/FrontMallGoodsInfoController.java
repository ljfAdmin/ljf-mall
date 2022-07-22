package com.ljf.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ljf.constant.FrontMallGoodsInfoConstant;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.vo.MallGoodsInfoDetailVO;
import com.ljf.entity.vo.SearchPageCategoryVO;
import com.ljf.service.MallGoodsCategoryService;
import com.ljf.service.MallGoodsInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class FrontMallGoodsInfoController {
    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Autowired
    private MallGoodsCategoryService mallGoodsCategoryService;

    /**
     * 跳转到商品信息页面，并携带数据，数据+分页条件实现
     * */
    @GetMapping({"/search", "/search.html"})
    public String searchPage(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        if(StringUtils.isEmpty((CharSequence) params.get("page"))){
            params.put("page", 1);
        }
        params.put("limit", FrontMallGoodsInfoConstant.GOODS_SEARCH_PAGE_LIMIT);

        // 封装分类数据
        // 如果包含goodsCategoryId请求参数，并且该请求参数传入值不为null以及空串
        if (params.containsKey("goodsCategoryId") && !StringUtils.isEmpty((String) params.get("goodsCategoryId"))) {
            Long categoryId = Long.valueOf((String) params.get("goodsCategoryId"));

            SearchPageCategoryVO searchPageCategoryVO = mallGoodsCategoryService.getCategoriesForSearch(categoryId);

            if (searchPageCategoryVO != null) {
                request.setAttribute("goodsCategoryId", categoryId);
                request.setAttribute("searchPageCategoryVO", searchPageCategoryVO);
            }
        }

        //封装参数供前端回显
        if (params.containsKey("orderBy") && !StringUtils.isEmpty((String) params.get("orderBy"))) {
            request.setAttribute("orderBy", (String) params.get("orderBy"));
        }

        String keyword = "";
        //对keyword做过滤 去掉空格
        if (params.containsKey("keyword") && !StringUtils.isEmpty(((String) (params.get("keyword"))).trim())) {
            keyword = (String) params.get("keyword");
        }
        request.setAttribute("keyword", keyword);
        params.put("keyword", keyword);
        //搜索上架状态下的商品
        params.put("goodsSellStatus", FrontMallGoodsInfoConstant.SELL_STATUS_UP);

        Integer currentPage = (Integer) params.get("page");
        Integer limit = (Integer) params.get("limit");
        Page<MallGoodsInfo> page = new Page<>(currentPage,limit);

        QueryWrapper<MallGoodsInfo> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)){
            queryWrapper.like("goods_name",keyword).or().like("goods_intro",keyword);
        }
        if(!StringUtils.isEmpty((String) params.get("goodsCategoryId"))){
            queryWrapper.eq("goods_category_id",Long.valueOf((String) params.get("goodsCategoryId")));
        }
        queryWrapper.eq("goods_sell_status",FrontMallGoodsInfoConstant.SELL_STATUS_UP);
        String orderBy = (String) params.get("orderBy");
        if("new".equals(orderBy)){
            // 按照发布时间倒序排列
            queryWrapper.orderByDesc("create_time");
        }else if("price".equals(orderBy)){
            // 按照售价从小到大排序
            queryWrapper.orderByAsc("selling_price");
        }else {
            // 默认按照库存数量从大到小排列
            queryWrapper.orderByDesc("stock_num");
        }

        mallGoodsInfoService.page(page,queryWrapper);

        request.setAttribute("pageResult", page);
        return "mall/search";
    }

    /**
     * 根据商品ID信息显示商品详情
     *
     * 上架商品信息！
     * */
    @GetMapping("/goods/detail/{goodsId}")
    public String detailPage(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) throws Exception {
        if (goodsId < 1) {
            throw new Exception("参数异常");
            // MallException.fail("参数异常");
        }

        MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(goodsId);
        if(FrontMallGoodsInfoConstant.SELL_STATUS_UP != goodsInfo.getGoodsSellStatus()){
            throw new Exception(ToFrontMessageConstantEnum.GOODS_NOT_EXIST.getResult());
        }

        MallGoodsInfoDetailVO goodsInfoDetailVO = new MallGoodsInfoDetailVO();
        BeanUtils.copyProperties(goodsInfo,goodsInfoDetailVO);

        goodsInfoDetailVO.setGoodsCarouselList(goodsInfo.getGoodsCarousel().split(","));
        request.setAttribute("goodsDetail", goodsInfoDetailVO);
        return "mall/detail";
    }

}
