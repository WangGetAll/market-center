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

    @DBRouter
    UserRaffleOrder queryNoUsedRaffleOrder(UserRaffleOrder userRaffleOrderReq);

}
