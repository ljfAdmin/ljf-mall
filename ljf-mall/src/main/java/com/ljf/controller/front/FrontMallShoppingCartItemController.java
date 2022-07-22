package com.ljf.controller.front;

import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.constant.ToFrontMessageConstantEnum;
import com.ljf.entity.MallShoppingCartItem;
import com.ljf.entity.vo.MallMyCouponVO;
import com.ljf.entity.vo.MallShoppingCartItemVO;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.service.MallCouponService;
import com.ljf.service.MallShoppingCartItemService;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class FrontMallShoppingCartItemController {
    @Autowired
    private MallShoppingCartItemService mallShoppingCartItemService;

    @Autowired
    private MallCouponService mallCouponService;

    /**
     * 携带数据跳转到 购物车页面     MallShoppingCartItemVO   itemsTotal   priceTotal
     * */
    @GetMapping("/shop-cart")
    public String toCartListPage(HttpServletRequest request,HttpSession httpSession) throws Exception {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<MallShoppingCartItemVO> myShoppingCartItems = mallShoppingCartItemService.getMyShoppingCartItems(user.getUserId());
        if (!CollectionUtils.isEmpty(myShoppingCartItems)) {
            // 购物项总数
            for (MallShoppingCartItemVO myShoppingCartItem : myShoppingCartItems) {
                itemsTotal += myShoppingCartItem.getGoodsCount();
            }
            if(itemsTotal < 1){
                throw new Exception("购物项不能为空");
            }

            // 总价     可以和上述循环合起来
            for (MallShoppingCartItemVO myShoppingCartItem : myShoppingCartItems) {
                priceTotal += myShoppingCartItem.getGoodsCount() * myShoppingCartItem.getSellingPrice();
            }

            if (priceTotal < 1) {
                throw new Exception("购物项价格异常");
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItems);
        return "mall/cart";
    }

    /**
     * 保存新的购物车数据
     * */
    @PostMapping("/shop-cart")
    @ResponseBody
    public Result saveMallShoppingCartItem(@RequestBody MallShoppingCartItem mallShoppingCartItem,
                                                 HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        mallShoppingCartItem.setUserId(user.getUserId());
        boolean saved = mallShoppingCartItemService.save(mallShoppingCartItem);

        return saved ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.SAVE_FAILED.getResult());
    }

    /**
     * 修改购物车信息
     * */
    @PutMapping("/shop-cart")
    @ResponseBody
    public Result updateMallShoppingCartItem(@RequestBody MallShoppingCartItem mallShoppingCartItem,
                                                   HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        mallShoppingCartItem.setUserId(user.getUserId());
        boolean updated = mallShoppingCartItemService.updateById(mallShoppingCartItem);

        return updated ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.UPDATED_FAILED.getResult());
    }

    /**
     * 删除购物车Item的数据
     * */
    @DeleteMapping("/shop-cart/{mallShoppingCartItemId}")
    @ResponseBody
    public Result deleteMallShoppingCartItem(@PathVariable("mallShoppingCartItemId") Long mallShoppingCartItemId,
                                                   HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        boolean deleted = mallShoppingCartItemService.deleteByIdAndUserId(mallShoppingCartItemId, user.getUserId());

        return deleted ? ResultGenerator.genSuccessResult() : ResultGenerator.genFailResult(ToFrontMessageConstantEnum.DELETE_FAILED.getResult());
    }

    /**
     * settle：解决
     *  跳转到结算页面
     * ？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？
     * */
    @GetMapping("/shop-cart/settle")
    public String settlePage(HttpServletRequest request,HttpSession httpSession) throws Exception {
        int priceTotal = 0;
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        List<MallShoppingCartItemVO> myShoppingCartItemVOs = mallShoppingCartItemService.getMyShoppingCartItems(user.getUserId());

        if (CollectionUtils.isEmpty(myShoppingCartItemVOs)) {
            //无数据则不跳转至结算页
            return "/shop-cart";
        } else {
            //总价
            for (MallShoppingCartItemVO myShoppingCartItemVO : myShoppingCartItemVOs) {
                priceTotal += myShoppingCartItemVO.getGoodsCount() * myShoppingCartItemVO.getSellingPrice();
            }

            if (priceTotal < 1) {
                throw new Exception("购物项价格异常");
            }
        }
        List<MallMyCouponVO> myCouponVOS = mallCouponService.selectOrderCanUseCoupons(myShoppingCartItemVOs, priceTotal, user.getUserId());

        request.setAttribute("coupons", myCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", myShoppingCartItemVOs);
        return "mall/order-settle";
    }

}
