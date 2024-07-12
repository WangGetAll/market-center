package com.wjy.marketcenter.dao.activity;
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

    /**
     * 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数
     * @param raffleActivityAccountReq
     * @return
     */
    @DBRouter
    RaffleActivityAccount queryActivityAccountByUserId(RaffleActivityAccount raffleActivityAccountReq);

    /**
     *  根据userId、activityId、总/月/日剩余抽奖次数大于0更新raffle_activity_account表，将总/月/日剩余抽奖次数都减一。
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

    RaffleActivityAccount queryAccountByUserId(RaffleActivityAccount raffleActivityAccount);


}
