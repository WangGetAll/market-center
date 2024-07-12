package com.wjy.marketcenter.repository.award;

import com.wjy.marketcenter.aggregate.award.GiveOutPrizesAggregate;
import com.wjy.marketcenter.aggregate.award.UserAwardRecordAggregate;

/**
 * 奖品仓储服务
 */
public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);
    String queryAwardConfig(Integer awardId);

    void saveGiveOutPrizesAggregate(GiveOutPrizesAggregate giveOutPrizesAggregate);

    /**
     * 根据awardId查询award表，得到awardKey
     * @param awardId
     * @return
     */
    String queryAwardKey(Integer awardId);


}
