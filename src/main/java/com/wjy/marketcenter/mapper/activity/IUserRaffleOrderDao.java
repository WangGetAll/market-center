package com.wjy.marketcenter.mapper.activity;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.wjy.marketcenter.po.activity.UserRaffleOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户抽奖订单表。用户点了抽奖会领取活动，领取活动会生成用户抽奖订单
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserRaffleOrderDao {

    void insert(UserRaffleOrder userRaffleOrder);

    /**
     * 根据userId、activityId、order_state(创建态)查user_raffle_order表，得到用户的抽奖单
     * @param userRaffleOrderReq
     * @return
     */
    @DBRouter
    UserRaffleOrder queryNoUsedRaffleOrder(UserRaffleOrder userRaffleOrderReq);

    /**
     * 根据用户id、订单id、抽奖单状态为create更新user_raffle_order表，将状态改为used
     * @param userRaffleOrderReq
     * @return
     */
    int updateUserRaffleOrderStateUsed(UserRaffleOrder userRaffleOrderReq);

}
