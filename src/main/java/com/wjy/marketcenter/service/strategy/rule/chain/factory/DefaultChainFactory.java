package com.wjy.marketcenter.service.strategy.rule.chain.factory;

import com.wjy.marketcenter.entity.StrategyEntity;
import com.wjy.marketcenter.repository.strategy.StrategyRepository;
import com.wjy.marketcenter.service.strategy.rule.chain.ILogicChain;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 工厂
 */
@Service
public class DefaultChainFactory {
    private final Map<String, ILogicChain> logicChainGroup;
    protected StrategyRepository repository;

    public DefaultChainFactory(Map<String, ILogicChain> logicChainGroup, StrategyRepository repository) {
        this.logicChainGroup = logicChainGroup;
        this.repository = repository;
    }

    /**
     * 1. 根据strategyId查strategy表，获得该策略配置的规则
     * 2. 装配责任链
     *  2.1. 如果没有给策略配置规则，则只装填一个默认责任链，返回
     *  2.2.
     * @param strategyId 策略ID
     * @return LogicChain
     */
    public ILogicChain openLogicChain(Long strategyId) {
        // 根据strategyId查strategy表，获得该策略配置的规则
        StrategyEntity strategy = repository.queryStrategyEntityByStrategyId(strategyId);
        String[] ruleModels = strategy.ruleModels();

        // 如果未配置策略规则，则只装填一个默认责任链
        if (null == ruleModels || 0 == ruleModels.length) return logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode());

        // 按照配置顺序装填用户配置的责任链；rule_blacklist、rule_weight 「注意此数据从Redis缓存中获取，如果更新库表，记得在测试阶段手动处理缓存」
        ILogicChain logicChain = logicChainGroup.get(ruleModels[0]);
        ILogicChain current = logicChain;
        for (int i = 1; i < ruleModels.length; i++) {
            ILogicChain nextChain = logicChainGroup.get(ruleModels[i]);
            current = current.appendNext(nextChain);
        }

        // 责任链的最后装填默认责任链
        current.appendNext(logicChainGroup.get(LogicModel.RULE_DEFAULT.getCode()));

        return logicChain;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardVO {
        /** 抽奖奖品ID - 内部流转使用 */
        private Integer awardId;
        /**
         *
         * 抽奖类型；黑名单抽奖、权重规则、默认抽奖
         */

        private String logicModel;
        /**
         * 抽奖奖品规则
         */
        private String awardRuleValue;

    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_DEFAULT("rule_default", "默认抽奖"),
        RULE_BLACKLIST("rule_blacklist", "黑名单抽奖"),
        RULE_WEIGHT("rule_weight", "权重规则"),
        ;

        private final String code;
        private final String info;

    }

}
