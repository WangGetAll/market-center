package com.wjy.marketcenter.service.strategy.armory;

import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.entity.StrategyEntity;
import com.wjy.marketcenter.entity.StrategyRuleEntity;
import com.wjy.marketcenter.enums.ResponseCode;
import com.wjy.marketcenter.exception.AppException;
import com.wjy.marketcenter.repository.strategy.StrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class StrategyArmoryDispatch implements IStrategyArmory, IStrategyDispatch {

    @Resource
    private StrategyRepository repository;

    private final SecureRandom secureRandom = new SecureRandom();


    /**
     * 根据activityId查询出strategyId。然后根据策略id。
     * 缓存奖品库存， key:strategyId_awardId,value:库存
     * 缓存该策略下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
     * 缓存该策略下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
     *
     * @param activityId
     * @return
     */
    @Override
    public boolean assembleLotteryStrategyByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);
        return assembleLotteryStrategy(strategyId);
    }

    /**
     * 缓存奖品库存， key:strategyId_awardId,value:库存
     * 缓存该策略下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
     * 缓存该策略下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
     *
     * @param strategyId
     * @return
     */
    @Override
    public boolean assembleLotteryStrategy(Long strategyId) {
        //  查询策略对应的奖品信息
        List<StrategyAwardEntity> strategyAwardEntities = repository.queryStrategyAwardList(strategyId);
        // 缓存奖品库存【用于decr扣减库存使用】
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            Integer awardCount = strategyAward.getAwardCount();
            cacheStrategyAwardCount(strategyId, awardId, awardCount);
        }

        //  缓存该策略下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
        //  缓存该策略下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
        assembleLotteryStrategy(String.valueOf(strategyId), strategyAwardEntities);

        // 查询策略信息配置的规则信息
        StrategyEntity strategyEntity = repository.queryStrategyEntityByStrategyId(strategyId);
        // 获取策略的权重规则
        String ruleWeight = strategyEntity.getRuleWeight();
        if (null == ruleWeight) return true;
        // 查询此策略的权重规则的具体配置
        StrategyRuleEntity strategyRuleEntity = repository.queryStrategyRule(strategyId, ruleWeight);
        if (null == strategyRuleEntity) {
            throw new AppException(ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getCode(), ResponseCode.STRATEGY_RULE_WEIGHT_IS_NULL.getInfo());
        }
        // 解析具体配置到map。key：积分：奖品id1，奖品id2...，value：可以抽的奖品id的List
        Map<String, List<Integer>> ruleWeightValueMap = strategyRuleEntity.getRuleWeightValues();
        for (String key : ruleWeightValueMap.keySet()) {
            List<Integer> ruleWeightValues = ruleWeightValueMap.get(key);
            ArrayList<StrategyAwardEntity> strategyAwardEntitiesClone = new ArrayList<>(strategyAwardEntities);
            // 去掉权重规则下，不存在的奖品信息
            strategyAwardEntitiesClone.removeIf(entity -> !ruleWeightValues.contains(entity.getAwardId()));
            // 缓存该策略+积分下下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
            // 缓存该策略+积分下下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
            assembleLotteryStrategy(String.valueOf(strategyId).concat("_").concat(key), strategyAwardEntitiesClone);
        }

        return true;

    }




    /**
     * 缓存该策略下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
     * 缓存该策略下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
     *
     * @param key                   策略id或者策略id_积分：奖品id,奖品id,奖品id
     * @param strategyAwardEntities 策略下的奖品信息
     * @return
     */
    private boolean assembleLotteryStrategy(String key, List<StrategyAwardEntity> strategyAwardEntities) {
        // 1. 获取最小概率值
        BigDecimal minAwardRate = strategyAwardEntities.stream()
                .map(StrategyAwardEntity::getAwardRate)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        // 2. 获取概率值总和
        // 2. 循环计算找到概率范围值
        BigDecimal rateRange = BigDecimal.valueOf(convert(minAwardRate.doubleValue()));

        // 3 生成策略奖品概率查找表「这里指需要在list集合中，存放上对应的奖品占位即可，占位越多等于概率越高」
        List<Integer> strategyAwardSearchRateTables = new ArrayList<>(rateRange.intValue());
        for (StrategyAwardEntity strategyAward : strategyAwardEntities) {
            Integer awardId = strategyAward.getAwardId();
            BigDecimal awardRate = strategyAward.getAwardRate();
            // 计算出每个概率值需要存放到查找表的数量，循环填充
            for (int i = 0; i < rateRange.multiply(awardRate).intValue(); i++) {
                strategyAwardSearchRateTables.add(awardId);
            }
        }

        // 4. 对存储的奖品进行乱序操作
        Collections.shuffle(strategyAwardSearchRateTables);
        // 6. 生成出Map集合，key值，对应的就是后续的概率值。通过概率来获得对应的奖品ID
        Map<Integer, Integer> shuffleStrategyAwardSearchRateTable = new LinkedHashMap<>();
        for (int i = 0; i < strategyAwardSearchRateTables.size(); i++) {
            shuffleStrategyAwardSearchRateTable.put(i, strategyAwardSearchRateTables.get(i));
        }
        // 7. 存放到 Redis
        repository.storeStrategyAwardSearchRateTable(key, shuffleStrategyAwardSearchRateTable.size(), shuffleStrategyAwardSearchRateTable);
        return true;
    }


    /**
     * 转换计算，只根据小数位来计算。如【0.01返回100】、【0.009返回1000】、【0.0018返回10000】
     */
    private double convert(double min) {
        if (0 == min) return 1D;

        double current = min;
        double max = 1;
        while (current < 1) {
            current = current * 10;
            max = max * 10;
        }
        return max;
    }

    /**
     * 缓存奖品库存到Redis
     * key为strategyId_awardId
     * value为此策略下该奖品的库存
     *
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @param awardCount 奖品库存
     */
    private void cacheStrategyAwardCount(Long strategyId, Integer awardId, Integer awardCount) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        repository.cacheStrategyAwardCount(cacheKey, awardCount);
    }
    /**
     * 在某种策略下，生成随机数，进而获得抽奖结果（奖品id）
     *
     * @param strategyId
     * @return
     */
    @Override
    public Integer getRandomAwardId(Long strategyId) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(strategyId);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(String.valueOf(strategyId), new SecureRandom().nextInt(rateRange));
    }


    /**
     * 在某种策略下，生成随机数，进而获得抽奖结果（奖品id）
     *
     * @param strategyId
     * @return
     */
    @Override
    public Integer getRandomAwardId(Long strategyId, String ruleWeightValue) {
        String key = String.valueOf(strategyId).concat("_").concat(ruleWeightValue);
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, new SecureRandom().nextInt(rateRange));

    }

    @Override
    public Integer getRandomAwardId(String key) {
        // 分布式部署下，不一定为当前应用做的策略装配。也就是值不一定会保存到本应用，而是分布式应用，所以需要从 Redis 中获取。
        int rateRange = repository.getRateRange(key);
        // 通过生成的随机值，获取概率值奖品查找表的结果
        return repository.getStrategyAwardAssemble(key, secureRandom.nextInt(rateRange));
    }


    /**
     * 扣减库存
     *
     * @param strategyId
     * @param awardId
     * @return
     */
    @Override
    public Boolean subtractionAwardStock(Long strategyId, Integer awardId, Date endDateTime) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_KEY + strategyId + Constants.UNDERLINE + awardId;
        return repository.subtractionAwardStock(cacheKey, endDateTime);
    }




}