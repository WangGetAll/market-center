package com.wjy.marketcenter.service.award;

import com.wjy.marketcenter.entity.award.DistributeAwardEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;

/**
 * 奖品服务接口
 */
public interface IAwardService {

    void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity);

    /**
     * 配送发货奖品
     */
    void distributeAward(DistributeAwardEntity distributeAwardEntity);


}

