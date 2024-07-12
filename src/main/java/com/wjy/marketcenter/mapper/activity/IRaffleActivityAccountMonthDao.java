package com.wjy.marketcenter.mapper.activity;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.wjy.marketcenter.po.activity.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-月次数
 */
@Mapper
public interface IRaffleActivityAccountMonthDao {
    /**
     * 根据userId、activityId、month查raffle_activity_account_month表，获得用户在该活动上某月的抽奖次数
     * @param raffleActivityAccountMonthReq
     * @return
     */
    @DBRouter
    RaffleActivityAccountMonth queryActivityAccountMonthByUserId(RaffleActivityAccountMonth raffleActivityAccountMonthReq);

    /**
     * 根据userId、activityId、month、month_count_surplus>0更新raffle_activity_account_month表，将month_count_surplus减一
     * @param raffleActivityAccountMonth
     * @return
     */
    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);

    /**
     * 增加月次数记录
     * @param raffleActivityAccountMonth
     */
    void insertActivityAccountMonth(RaffleActivityAccountMonth raffleActivityAccountMonth);

    void addAccountQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);


}
