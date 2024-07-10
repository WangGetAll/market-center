package com.wjy.marketcenter.mapper.activity;
import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.wjy.marketcenter.po.activity.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 *  抽奖活动账户表Dao
 */
@Mapper
public interface IRaffleActivityAccountDao {
    void insert(RaffleActivityAccount raffleActivityAccount);

    int updateAccountQuota(RaffleActivityAccount raffleActivityAccount);

    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserId(RaffleActivityAccount raffleActivityAccountReq);

    /**
     *  减少用户在某个活动上的总剩余抽奖次数
     * @param raffleActivityAccount
     * @return
     */
    int updateActivityAccountSubtractionQuota(RaffleActivityAccount raffleActivityAccount);

    /**
     * 减少用户在某个活动上的月剩余抽奖次数
     * @param raffleActivityAccount
     */
    void updateActivityAccountMonthSurplusImageQuota(RaffleActivityAccount raffleActivityAccount);

    void updateActivityAccountDaySurplusImageQuota(RaffleActivityAccount raffleActivityAccount);


}
