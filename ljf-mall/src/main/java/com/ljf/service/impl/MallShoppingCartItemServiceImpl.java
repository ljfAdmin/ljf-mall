package com.ljf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallShoppingCartItem;
import com.ljf.entity.vo.MallShoppingCartItemVO;
import com.ljf.mapper.MallShoppingCartItemMapper;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.service.MallShoppingCartItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ljf
 * @since 2022-06-24
 */
@Service
public class MallShoppingCartItemServiceImpl extends ServiceImpl<MallShoppingCartItemMapper, MallShoppingCartItem> implements MallShoppingCartItemService {
    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Override
    public List<MallShoppingCartItemVO> getMyShoppingCartItems(Long userId) {
        List<MallShoppingCartItemVO> ans = new ArrayList<>();

        QueryWrapper<MallShoppingCartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<MallShoppingCartItem> shoppingCartItems = baseMapper.selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(shoppingCartItems)) {
            for (MallShoppingCartItem shoppingCartItem : shoppingCartItems) {
                MallShoppingCartItemVO shoppingCartItemVO = new MallShoppingCartItemVO();
                BeanUtils.copyProperties(shoppingCartItem,shoppingCartItemVO);

                MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(shoppingCartItem.getGoodsId());
                shoppingCartItemVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
                String goodsName = goodsInfo.getGoodsName();
                // 字符串过长导致文字超出的问题
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                }
                shoppingCartItemVO.setGoodsName(goodsName);
                shoppingCartItemVO.setSellingPrice(goodsInfo.getSellingPrice());

                ans.add(shoppingCartItemVO);
            }
        }

        return ans;
    }

    /**
     * 根据购物车Item的id删除，并判断是否属于当前用户
     * */
    @Override
    public boolean deleteByIdAndUserId(Long mallShoppingCartItemId, Long userId) {
        MallShoppingCartItem mallShoppingCartItem = baseMapper.selectById(mallShoppingCartItemId);
        if(mallShoppingCartItem == null){
            return false;
        }

        // userId不同不能删除
        if(!userId.equals(mallShoppingCartItem.getUserId())){
            return false;
        }

        return baseMapper.deleteById(mallShoppingCartItemId) > 0;
    }
}
