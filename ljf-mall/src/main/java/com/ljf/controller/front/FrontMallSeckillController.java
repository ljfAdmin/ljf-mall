package com.ljf.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.FrontMallUserInfoConstant;
import com.ljf.constant.MallSeckillInfoConstant;
import com.ljf.entity.MallGoodsInfo;
import com.ljf.entity.MallSeckill;
import com.ljf.entity.vo.MallSeckillGoodsVO;
import com.ljf.entity.vo.MallSeckillSuccessVO;
import com.ljf.entity.vo.MallUserVO;
import com.ljf.entity.vo.SeckillInterfaceExposeVO;
import com.ljf.redis.MallRedisCache;
import com.ljf.service.MallGoodsInfoService;
import com.ljf.service.MallSeckillService;
import com.ljf.service.MallSeckillSuccessService;
import com.ljf.utils.BeanUtil;
import com.ljf.utils.MD5Util;
import com.ljf.utils.Result;
import com.ljf.utils.ResultGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Api(description = "前台秒杀相关控制层类")
@Controller
public class FrontMallSeckillController {
    @Autowired
    private MallSeckillService mallSeckillService;
    @Autowired
    private MallSeckillSuccessService mallSeckillSuccessService;

    @Autowired
    private MallGoodsInfoService mallGoodsInfoService;

    @Autowired
    private MallRedisCache mallRedisCache;

    @ApiOperation(value = "跳转到秒杀列表页面")
    @GetMapping("/seckill")
    public String toSeckillIndex() {
        return "mall/seckill-list";
    }

    /**
     * 获取服务器时间
     */
    @ApiOperation(value = "获取当前时间（秒杀）")
    @ResponseBody
    @GetMapping("/seckill/time/now")
    public Result getTimeNow() {
        return ResultGenerator.genSuccessResult(new Date().getTime());
    }

    /**
     * 判断秒杀商品虚拟库存是否足够
     * @param seckillId 秒杀ID
     */
    @ApiOperation(value = "判断秒杀商品虚拟库存是否足够")
    @ResponseBody
    @PostMapping("/seckill/{seckillId}/checkStock")
    public Result seckillCheckStock(@PathVariable("seckillId") Long seckillId) {
        // seckill_goods_stock:
        Integer stock = mallRedisCache.getCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_STOCK_KEY + seckillId);
        if (stock == null || stock < 0) {
            return ResultGenerator.genFailResult("秒杀商品库存不足");
        }
        // redis虚拟库存大于等于0时，可以执行秒杀
        return ResultGenerator.genSuccessResult();
    }

    /**
     * 获取秒杀链接
     *
     * @param seckillId 秒杀商品ID
     * @return result
     */
    @ApiOperation(value = "获取秒杀连接，包括秒杀状态、系统当前时间、开始时间、结束时间、加密和ID")
    @ResponseBody
    @PostMapping("/seckill/{seckillId}/exposer")
    public Result exposerUrl(@PathVariable Long seckillId) {
        SeckillInterfaceExposeVO exposerVO = mallSeckillService.exposerUrl(seckillId);

        return ResultGenerator.genSuccessResult(exposerVO);
    }

    /**
     * 使用限流注解进行接口限流操作
     *  并返回秒杀成功VO对象
     *
     * @param seckillId 秒杀ID
     * @param userId    用户ID
     * @param md5       秒杀链接的MD5加密信息
     */
    @ApiOperation(value = "执行秒杀操作")
    @ResponseBody
    @PostMapping(value = "/seckillExecution/{seckillId}/{userId}/{md5}")
    public Result execute(@PathVariable("seckillId") Long seckillId,
                          @PathVariable("userId") Long userId,
                          @PathVariable("md5") String md5) throws Exception {
        // 判断md5信息是否合法
        if (md5 == null || userId == null || !md5.equals(MD5Util.encrypt(seckillId.toString()))) {
            throw new Exception("秒杀商品不存在");
        }
        MallSeckillSuccessVO seckillSuccessVO = mallSeckillService.executeSeckill(seckillId, userId);
        return ResultGenerator.genSuccessResult(seckillSuccessVO);
    }

    /**
     * 根据秒杀ID跳转到秒杀信息页面
     * */
    @ApiOperation(value = "根据秒杀ID跳转到秒杀信息页面")
    @GetMapping("/seckill/info/{seckillId}")
    public String toSeckillInfo(@PathVariable("seckillId") Long seckillId,
                              HttpServletRequest request,
                              HttpSession httpSession) {
        MallUserVO user = (MallUserVO) httpSession.getAttribute(FrontMallUserInfoConstant.MALL_USER_SESSION_KEY);
        if (user != null) {
            request.setAttribute("userId", user.getUserId());
        }
        request.setAttribute("seckillId", seckillId);
        return "mall/seckill-detail";
    }

    /**
     * 秒杀列表
     * */
    @ApiOperation(value = "前台：秒杀列表信息")
    @GetMapping("/seckill/list")
    @ResponseBody
    public Result secondKillGoodsList() {
        // 直接返回配置的秒杀商品列表
        // 不返回商品id，每配置一条秒杀数据，就生成一个唯一的秒杀id和发起秒杀的事件id，
        // 根据秒杀id去访问详情页    seckill_goods_list
        List<MallSeckillGoodsVO> mallSeckillGoodsVOS = mallRedisCache.getCacheObject(
                MallSeckillInfoConstant.SECKILL_GOODS_LIST);

        // List<MallSeckillGoodsVO> seckillGoodsVOS = new ArrayList<>();
        if(CollectionUtils.isEmpty(mallSeckillGoodsVOS)){
            mallSeckillGoodsVOS = new ArrayList<>();

            QueryWrapper<MallSeckill> queryWrapper = new QueryWrapper<>();
            // 秒杀商品状态（0下架，1上架）
            queryWrapper.eq("seckill_status",1);
            queryWrapper.gt("seckill_num",0);
            queryWrapper.orderByDesc("seckill_rank");
            queryWrapper.last("limit 10");
            List<MallSeckill> seckills = mallSeckillService.list(queryWrapper);

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");

            for (MallSeckill seckill : seckills) {
                MallSeckillGoodsVO mallSeckillGoodsVO = new MallSeckillGoodsVO();
                BeanUtils.copyProperties(seckill,mallSeckillGoodsVO);
                MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(seckill.getGoodsId());
                if(goodsInfo == null){
                    return null;
                }

                mallSeckillGoodsVO.setGoodsName(goodsInfo.getGoodsName());
                mallSeckillGoodsVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());

                mallSeckillGoodsVO.setGoodsDetailContent(goodsInfo.getGoodsDetailContent());
                mallSeckillGoodsVO.setGoodsIntro(goodsInfo.getGoodsIntro());

                mallSeckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());

                Date seckillBegin = mallSeckillGoodsVO.getSeckillBegin();
                Date seckillEnd = mallSeckillGoodsVO.getSeckillEnd();
                String formatBegin = sdf.format(seckillBegin);
                String formatEnd = sdf.format(seckillEnd);
                mallSeckillGoodsVO.setSeckillBeginTime(formatBegin);
                mallSeckillGoodsVO.setSeckillEndTime(formatEnd);

                mallSeckillGoodsVOS.add(mallSeckillGoodsVO);
            }

            mallRedisCache.setCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_LIST, mallSeckillGoodsVOS,
                    60 * 60 * 100, TimeUnit.SECONDS);
        }

        return ResultGenerator.genSuccessResult(mallSeckillGoodsVOS);
    }

    /**
     * 根据秒杀ID获取  对应的VO对象
     * */
    @ApiOperation(value = "根据秒杀ID获得对应的VO对象")
    @GetMapping("/seckill/{seckillId}")
    @ResponseBody
    public Result seckillGoodsDetail(@PathVariable Long seckillId) {
        // 返回秒杀商品详情VO，如果秒杀时间未到，不允许访问详情页，也不允许返回数据，参数为秒杀id
        // 根据返回的数据解析出秒杀的事件id，发起秒杀
        // 不访问详情页不会获取到秒杀的事件id，不然容易被猜到url路径从而直接发起秒杀请求
        MallSeckillGoodsVO mallSeckillGoodsVO = mallRedisCache.getCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_DETAIL + seckillId);
        if (mallSeckillGoodsVO == null) {
            MallSeckill mallSeckill = mallSeckillService.getById(seckillId);

            // 秒杀商品状态（0下架，1上架）false下架，true上架
            if (!mallSeckill.getSeckillStatus()) {
                return ResultGenerator.genFailResult("秒杀商品已下架");
            }
            mallSeckillGoodsVO = new MallSeckillGoodsVO();
            BeanUtil.copyProperties(mallSeckill,mallSeckillGoodsVO);
            MallGoodsInfo goodsInfo = mallGoodsInfoService.getById(mallSeckill.getGoodsId());
            mallSeckillGoodsVO.setGoodsName(goodsInfo.getGoodsName());
            mallSeckillGoodsVO.setGoodsIntro(goodsInfo.getGoodsIntro());
            mallSeckillGoodsVO.setGoodsDetailContent(goodsInfo.getGoodsDetailContent());
            mallSeckillGoodsVO.setGoodsCoverImg(goodsInfo.getGoodsCoverImg());
            mallSeckillGoodsVO.setSellingPrice(goodsInfo.getSellingPrice());

            Date seckillBegin = mallSeckillGoodsVO.getSeckillBegin();
            Date seckillEnd = mallSeckillGoodsVO.getSeckillEnd();
            mallSeckillGoodsVO.setStartDate(seckillBegin.getTime());
            mallSeckillGoodsVO.setEndDate(seckillEnd.getTime());
            mallRedisCache.setCacheObject(MallSeckillInfoConstant.SECKILL_GOODS_DETAIL + seckillId, mallSeckillGoodsVO);
        }
        return ResultGenerator.genSuccessResult(mallSeckillGoodsVO);
    }

}
