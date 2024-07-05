package com.wjy.marketcenter.service.raffle;

import com.wjy.marketcenter.service.AbstractRaffleStrategy;
import com.wjy.marketcenter.service.rule.chain.ILogicChain;
import com.wjy.marketcenter.service.rule.chain.factory.DefaultChainFactory;
import com.wjy.marketcenter.service.rule.tree.factory.DefaultTreeFactory;
import com.wjy.marketcenter.service.rule.tree.factory.engine.IDecisionTreeEngine;
import com.wjy.marketcenter.valobj.RuleTreeVO;
import com.wjy.marketcenter.valobj.StrategyAwardRuleModelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



/**
 *  默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy {

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);

    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if (null == strategyAwardRuleModelVO) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return treeEngine.process(userId, strategyId, awardId);
    }
}
