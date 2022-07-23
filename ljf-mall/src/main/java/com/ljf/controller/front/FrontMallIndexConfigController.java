package com.ljf.controller.front;

import com.ljf.constant.FrontIndexConstant;
import com.ljf.entity.vo.MallIndexCarouselVO;
import com.ljf.entity.vo.MallIndexConfigGoodsVO;
import com.ljf.entity.vo.MallIndexGoodsCategoryVO;
import com.ljf.service.MallCarouselService;
import com.ljf.service.MallGoodsCategoryService;
import com.ljf.service.MallIndexConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class FrontMallIndexConfigController {
    @Autowired
    private MallIndexConfigService mallIndexConfigService;

    @Autowired
    private MallGoodsCategoryService mallGoodsCategoryService;

    @Autowired
    private MallCarouselService mallCarouselService;

    /**
     * 跳转到前台首页，同时带数据过去
     *  获取商品分类部分信息显示给前台页面
     *  获取热销商品信息显示给前台页面
     *  获取推荐商品信息显示给前台页面
     *  获取上新商品信息显示给前台页面
     *  获取轮播图信息显示给前台页面
     * */
    @GetMapping({"/index", "/", "/index.html"})
    public String indexPage(HttpServletRequest request) throws Exception {
        //long start = System.currentTimeMillis();
        List<MallIndexGoodsCategoryVO> categoryVOS = mallGoodsCategoryService.getCategoriesForIndex();
        //long cost = System.currentTimeMillis() - start;
        //System.out.println("查询三级分类数据消耗的时间：" + cost);
        /*for (MallIndexGoodsCategoryVO categoryVO : categoryVOS) {
            System.out.println(categoryVO);
        }*/

        if (CollectionUtils.isEmpty(categoryVOS)) {
            throw new Exception("分类数据不完善");
        }

        // 返回固定数量的轮播图对象(首页调用)
        List<MallIndexCarouselVO> carousels = mallCarouselService.getCarouselsForIndex(FrontIndexConstant.INDEX_CAROUSEL_NUMBER);
        // 返回固定数量的商品信息显示在首页
        List<MallIndexConfigGoodsVO> hotGoods = mallIndexConfigService.getConfigHotGoodsForIndex();// 参数是固定的，这里就不传入了
        List<MallIndexConfigGoodsVO> newGoods = mallIndexConfigService.getConfigNewGoodsForIndex();
        List<MallIndexConfigGoodsVO> recommendGoods = mallIndexConfigService.getConfigRecommendGoodsForIndex();

        request.setAttribute("categories", categoryVOS);//分类数据

        request.setAttribute("carousels", carousels);//轮播图
        request.setAttribute("hotGoodses", hotGoods);//热销商品
        request.setAttribute("newGoodses", newGoods);//新品
        request.setAttribute("recommendGoodses", recommendGoods);//推荐商品
        return "mall/index";
    }
}
