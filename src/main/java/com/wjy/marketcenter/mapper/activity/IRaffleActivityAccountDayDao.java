package com.wjy.marketcenter.mapper.activity;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.wjy.marketcenter.po.activity.RaffleActivityAccountDay;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-日次数
 */
@Mapper
public interface IRaffleActivityAccountDayDao {
    /**
     * 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数
     * @param raffleActivityAccountDayReq
     * @return
     */
    @DBRouter
    RaffleActivityAccountDay queryActivityAccountDayByUserId(RaffleActivityAccountDay raffleActivityAccountDayReq);

    int updateActivityAccountDaySubtractionQuota(RaffleActivityAccountDay raffleActivityAccountDay);

    void insertActivityAccountDay(RaffleActivityAccountDay raffleActivityAccountDay);

    /**
     * 根据用户id、活动id、day查询用户的日参与次数（总次数-剩余次数）
     * @param raffleActivityAccountDay
     * @return
     */
    @DBRouter
    Integer queryRaffleActivityAccountDayPartakeCount(RaffleActivityAccountDay raffleActivityAccountDay);


}
