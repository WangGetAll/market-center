package com.wjy.marketcenter.dao.activity;

import com.wjy.marketcenter.po.activity.RaffleActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动表Dao
 */
@Mapper
public interface IRaffleActivityDao {

    /**
     * 根据activityId查表raffle_activity,获得活动信息
     * @param activityId
     * @return
     */
    RaffleActivity queryRaffleActivityByActivityId(Long activityId);

    /**
     * 根据activityId查询raffle_activity表，获得StrategyId
     * @param activityId
     * @return
     */
    Long queryStrategyIdByActivityId(Long activityId);

    /**
     * 根据策略id，查raffle_activity表，得到活动id
     * @param strategyId
     * @return
     */
    Long queryActivityIdByStrategyId(Long strategyId);


}
