package com.wjy.marketcenter.service.strategy;

import com.wjy.marketcenter.entity.RaffleAwardEntity;
import com.wjy.marketcenter.entity.RaffleFactorEntity;

public interface IRaffleStrategy {

    /**
     *
     *
     * @param raffleFactorEntity 抽奖因子实体对象，根据入参信息计算抽奖结果
     * @return 抽奖的奖品
     */
    RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity);

}

