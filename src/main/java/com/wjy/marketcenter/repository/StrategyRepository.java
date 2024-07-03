package com.wjy.marketcenter.repository;

import javax.annotation.Resource;

import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.mapper.StrategyAwardMapper;
import com.wjy.marketcenter.po.StrategyAward;
import com.wjy.marketcenter.redis.RedisService;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class StrategyRepository  {

    @Resource
    private StrategyAwardMapper strategyAwardMapper;
    @Resource
    private RedisService redisService;

    /**
     * 根據startegyId，查询该策略下有哪些奖品，每种奖品的库存、剩余库存、中奖概率
     * @param strategyId
     * @return
     */

    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 优先从redis中获取，策略对应的奖品信息
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        if (null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;
        // 缓存中没有，从mysql中获取策略对应的奖品信息
        List<StrategyAward> strategyAwards = strategyAwardMapper.queryStrategyAwardListByStrategyId(strategyId);
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());
        for (StrategyAward strategyAward : strategyAwards) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardCount(strategyAward.getAwardCount())
                    .awardCountSurplus(strategyAward.getAwardCountSurplus())
                    .awardRate(strategyAward.getAwardRate())
                    .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        // 加載到緩存
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    /**
     *
     * @param strategyId
     * @param rateRange
     * @param strategyAwardSearchRateTable
     */
    public void storeStrategyAwardSearchRateTable(Long strategyId, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
        // key:strategyId, value:当前策略对应的范围
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId, rateRange);
        // 2. 存储概率查找表
        // key:strategyId，value: hash(key:随机数，value:奖品id)
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId);
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    /**
     * 根据策略id和生成的随机数，从redis中获取奖品的id。
     * @param strategyId
     * @param rateKey
     * @return
     */
    public Integer getStrategyAwardAssemble(Long strategyId, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }

    /**
     *  根据策略id获取策略对应的抽奖范围
     * @param strategyId
     * @return
     */
    public int getRateRange(Long strategyId) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + strategyId);
    }

}
