package com.wjy.marketcenter.mapper.activity;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.wjy.marketcenter.po.activity.RaffleActivityAccountMonth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖活动账户表-月次数
 */
@Mapper
public interface IRaffleActivityAccountMonthDao {
    @DBRouter
    RaffleActivityAccountMonth queryActivityAccountMonthByUserId(RaffleActivityAccountMonth raffleActivityAccountMonthReq);

    int updateActivityAccountMonthSubtractionQuota(RaffleActivityAccountMonth raffleActivityAccountMonth);

    /**
     * 增加月次数记录
     * @param raffleActivityAccountMonth
     */
    void insertActivityAccountMonth(RaffleActivityAccountMonth raffleActivityAccountMonth);

}
