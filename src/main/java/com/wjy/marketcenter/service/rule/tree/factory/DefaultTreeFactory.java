package com.wjy.marketcenter.service.rule.tree.factory;

import com.wjy.marketcenter.service.rule.tree.ILogicTreeNode;
import com.wjy.marketcenter.service.rule.tree.factory.engine.IDecisionTreeEngine;
import com.wjy.marketcenter.service.rule.tree.factory.engine.impl.DecisionTreeEngine;
import com.wjy.marketcenter.valobj.RuleLogicCheckTypeVO;
import com.wjy.marketcenter.valobj.RuleTreeVO;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 规则树工厂
 */
@Service
public class DefaultTreeFactory {

    private final Map<String, ILogicTreeNode> logicTreeNodeGroup;

    public DefaultTreeFactory(Map<String, ILogicTreeNode> logicTreeNodeGroup) {
        this.logicTreeNodeGroup = logicTreeNodeGroup;
    }

    public IDecisionTreeEngine openLogicTree(RuleTreeVO ruleTreeVO) {
        return new DecisionTreeEngine(logicTreeNodeGroup, ruleTreeVO);
    }

    /**
     * 决策树结点处理结果实体
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TreeActionEntity {
        // 放行还是不放行
        private RuleLogicCheckTypeVO ruleLogicCheckType;
        private StrategyAwardData strategyAwardData;
    }

    /**
     * 决策树结点处理结果中的奖品信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StrategyAwardData {
        /**
         * 抽奖奖品ID - 内部流转使用
         */
        private Integer awardId;
        /**
         * 抽奖奖品规则
         */
        private String awardRuleValue;
    }

}
