package com.wjy.marketcenter.repository;

import javax.annotation.Resource;

import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.entity.StrategyEntity;
import com.wjy.marketcenter.entity.StrategyRuleEntity;
import com.wjy.marketcenter.mapper.StrategyAwardMapper;
import com.wjy.marketcenter.mapper.StrategyMapper;
import com.wjy.marketcenter.mapper.StrategyRuleMapper;
import com.wjy.marketcenter.po.Strategy;
import com.wjy.marketcenter.po.StrategyAward;
import com.wjy.marketcenter.po.StrategyRule;
import com.wjy.marketcenter.redis.RedisService;
import com.wjy.marketcenter.valobj.StrategyAwardRuleModelVO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class StrategyRepository  {

    @Resource
    private StrategyAwardMapper strategyAwardMapper;
    @Resource
    private StrategyRuleMapper strategyRuleMapper;

    @Resource
    private StrategyMapper strategyMapper;

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
     * @param key
     * @param rateRange
     * @param strategyAwardSearchRateTable
     */
    public void storeStrategyAwardSearchRateTable(String key, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
        // key:strategyId, value:当前策略对应的范围
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange);
        // 2. 存储概率查找表
        // key:strategyId，value: hash(key:随机数，value:奖品id)
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    /**
     * 根据策略id和生成的随机数，从redis中获取奖品的id。
     * @param strategyId
     * @param rateKey
     * @return
     */
    public Integer getStrategyAwardAssemble(String strategyId, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }

    /**
     *  根据策略id获取策略对应的抽奖范围
     * @param strategyId
     * @return
     */
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    /**
     *  根据策略id+权重规则获取策略对应的抽奖范围
     * @param key
     * @return
     */
    public int getRateRange(String key) {
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }


    /**
     * 根据策略id，查询策略信息
     * @param strategyId
     * @return
     */
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 查redis
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) return strategyEntity;
        // 查mysql
        Strategy strategy = strategyMapper.queryStrategyByStrategyId(strategyId);
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        // 存redis
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }


    /**
     * 根据策略id和规则，查询规则信息
     * @param strategyId
     * @param ruleModel
     * @return
     */
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRuleRes = strategyRuleMapper.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleRes.getStrategyId())
                .awardId(strategyRuleRes.getAwardId())
                .ruleType(strategyRuleRes.getRuleType())
                .ruleModel(strategyRuleRes.getRuleModel())
                .ruleValue(strategyRuleRes.getRuleValue())
                .ruleDesc(strategyRuleRes.getRuleDesc())
                .build();
    }

    /**
     * 根据策略id、奖品id（如果有）、规则类型，strategy_rule表中去查询规则的详情
     * @param strategyId
     * @param awardId
     * @param ruleModel
     * @return
     */
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
        return strategyRuleMapper.queryStrategyRuleValue(strategyRule);
    }

    /**
     * 根据策略id、奖品id，strategy_award表中查询规则的名字
     * @param awardId
     * @return
     */
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardMapper.queryStrategyAwardRuleModels(strategyAward);
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();

    }
}
