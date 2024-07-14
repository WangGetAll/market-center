package com.wjy.marketcenter.dao.activity;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.wjy.marketcenter.po.activity.RaffleActivityOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 抽奖活动单Dao
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IRaffleActivityOrderDao {

    @DBRouter(key = "userId")
    void insert(RaffleActivityOrder raffleActivityOrder);

    @DBRouter
    List<RaffleActivityOrder> queryRaffleActivityOrderByUserId(String userId);

    /**
     * 根据userId、outBusinessNo查询raffle_activity_order表，得到活动下单信息
     * @param raffleActivityOrderReq
     * @return
     */
    @DBRouter
    RaffleActivityOrder queryRaffleActivityOrder(RaffleActivityOrder raffleActivityOrderReq);

    /**
     * 根据userid、outBusinessNo、state=“wait_pay”更新raffle_activity_order表，将state更新为completed
     * @param raffleActivityOrderReq
     * @return
     */
    int updateOrderCompleted(RaffleActivityOrder raffleActivityOrderReq);


}
