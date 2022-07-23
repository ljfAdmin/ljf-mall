package com.ljf.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ljf.constant.enums.MallOrderStatusEnum;
import com.ljf.entity.MallOrder;
import com.ljf.service.MallOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * 定时任务脚本，系统启动时检查是否有超时订单
 * */
public class TaskStartupRunner implements ApplicationRunner {
    public static final Long UN_PAID_ORDER_EXPIRE_TIME = 30L;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MallOrderService mallOrderService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取系统中所有还处于未支付状态的订单
        QueryWrapper<MallOrder> queryWrapper = new QueryWrapper<>();
        // 订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭
        queryWrapper.eq("order_status", MallOrderStatusEnum.ORDER_PRE_PAY.getOrderStatus());
        List<MallOrder> orders = mallOrderService.list(queryWrapper);

        if(!CollectionUtils.isEmpty(orders)){
            for (MallOrder order : orders) {
                // 获取订单创建时间
                Date createTime = order.getCreateTime();
                /**
                 *     Instant类由一个静态的工厂方法now()可以返回当前时间戳
                 *     时间戳是包含日期和时间的，与java.util.Date很类似，事实上Instant就是类似JDK8以前的Date
                 *     Instant和Date这两个类可以进行转换
                 */
                Instant createInstant = createTime.toInstant();
                /**
                 *
                 *  https://www.1024sky.cn/blog/article/5187
                 * */
                ZoneId zoneId = ZoneId.systemDefault();
                LocalDateTime add = createInstant.atZone(zoneId).toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expire = add.plusMinutes(UN_PAID_ORDER_EXPIRE_TIME);

                if (expire.isBefore(now)) {
                    // 已经过期，则加入延迟队列立即执行
                    taskService.addTask(new OrderUnPaidTask(order.getOrderId(), 0));
                } else {
                    // 还没过期，则加入延迟队列
                    long delay = ChronoUnit.MILLIS.between(now, expire);
                    taskService.addTask(new OrderUnPaidTask(order.getOrderId(), delay));
                }
            }
        }
    }
}
