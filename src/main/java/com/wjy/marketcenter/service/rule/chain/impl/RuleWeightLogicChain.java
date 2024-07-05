package com.wjy.marketcenter.service.rule.chain.impl;

import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.repository.StrategyRepository;
import com.wjy.marketcenter.service.armory.StrategyArmory;
import com.wjy.marketcenter.service.rule.chain.AbstractLogicChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component("rule_weight")
public class RuleWeightLogicChain extends AbstractLogicChain {

    @Resource
    private StrategyRepository repository;

    @Resource
    protected StrategyArmory strategyArmory;

    // 根据用户ID查询用户抽奖消耗的积分值，本章节我们先写死为固定的值。后续需要从数据库中查询。
    public Long userScore = 0L;

    @Override
    public Integer logic(String userId, Long strategyId) {
        log.info("抽奖责任链-权重开始 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        // 查询规则详情
        String ruleValue = repository.queryStrategyRuleValue(strategyId, ruleModel());

        // 解析规则详情
        Map<Long, String> analyticalValueGroup = getAnalyticalValue(ruleValue);

        if (null == analyticalValueGroup || analyticalValueGroup.isEmpty()) return null;

        // 转换Keys值，并默认排序
        List<Long> analyticalSortedKeys = new ArrayList<>(analyticalValueGroup.keySet());
        Collections.sort(analyticalSortedKeys);
        // 用户的积分在哪个范围
        Long nextValue = analyticalSortedKeys.stream()
                .sorted(Comparator.reverseOrder())
                .filter(analyticalSortedKeyValue -> userScore >= analyticalSortedKeyValue)
                .findFirst()
                .orElse(null);

        // 权重抽奖
        if (null != nextValue) {
            Integer awardId = strategyArmory.getRandomAwardId(strategyId, analyticalValueGroup.get(nextValue));
            log.info("抽奖责任链-权重接管 userId: {} strategyId: {} ruleModel: {} awardId: {}", userId, strategyId, ruleModel(), awardId);
            return awardId;
        }
        // 过滤其他责任链
        log.info("抽奖责任链-权重放行 userId: {} strategyId: {} ruleModel: {}", userId, strategyId, ruleModel());
        return next().logic(userId, strategyId);
    }


    /**
     * 解析规则
     * 权重规则格式：积分:奖品id,奖品id 积分:奖品id,奖品id
     * @param ruleValue
     * @return key:4000 value:4000:102,103,104,105
     */
    private Map<Long, String> getAnalyticalValue(String ruleValue) {
        String[] ruleValueGroups = ruleValue.split(Constants.SPACE);
        Map<Long, String> ruleValueMap = new HashMap<>();
        for (String ruleValueKey : ruleValueGroups) {
            // 检查输入是否为空
            if (ruleValueKey == null || ruleValueKey.isEmpty()) {
                return ruleValueMap;
            }
            // 分割字符串以获取键和值
            String[] parts = ruleValueKey.split(Constants.COLON);
            if (parts.length != 2) {
                throw new IllegalArgumentException("rule_weight rule_rule invalid input format" + ruleValueKey);
            }
            ruleValueMap.put(Long.parseLong(parts[0]), ruleValueKey);
        }
        return ruleValueMap;
    }

    @Override
    protected String ruleModel() {
        return "rule_weight";
    }
}
