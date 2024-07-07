package com.wjy.marketcenter.service.raffle;

import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.service.AbstractRaffleStrategy;
import com.wjy.marketcenter.service.IRaffleAward;
import com.wjy.marketcenter.service.IRaffleStock;
import com.wjy.marketcenter.service.rule.chain.ILogicChain;
import com.wjy.marketcenter.service.rule.chain.factory.DefaultChainFactory;
import com.wjy.marketcenter.service.rule.tree.factory.DefaultTreeFactory;
import com.wjy.marketcenter.service.rule.tree.factory.engine.IDecisionTreeEngine;
import com.wjy.marketcenter.valobj.RuleTreeVO;
import com.wjy.marketcenter.valobj.StrategyAwardRuleModelVO;
import com.wjy.marketcenter.valobj.StrategyAwardStockKeyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 *  默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleAward, IRaffleStock {

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
    @Override
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        return repository.takeQueueValue();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        repository.updateStrategyAwardStock(strategyId, awardId);
    }

    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardList(Long strategyId) {
        return repository.queryStrategyAwardList(strategyId);
    }

}
