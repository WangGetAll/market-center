package com.wjy.marketcenter.service.distribute;

import com.wjy.marketcenter.entity.award.DistributeAwardEntity;

/**
 * 分发奖品接口
 */
public interface IDistributeAward {

    void giveOutPrizes(DistributeAwardEntity distributeAwardEntity);

}
