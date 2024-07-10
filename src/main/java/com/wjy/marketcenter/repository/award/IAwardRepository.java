package com.wjy.marketcenter.repository.award;

import com.wjy.marketcenter.aggregate.award.UserAwardRecordAggregate;

/**
 * 奖品仓储服务
 */
public interface IAwardRepository {

    void saveUserAwardRecord(UserAwardRecordAggregate userAwardRecordAggregate);

}
