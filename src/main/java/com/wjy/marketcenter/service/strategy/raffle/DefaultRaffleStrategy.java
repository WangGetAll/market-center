package com.wjy.marketcenter.service.strategy.raffle;

import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.service.strategy.AbstractRaffleStrategy;
import com.wjy.marketcenter.service.strategy.IRaffleAward;
import com.wjy.marketcenter.service.strategy.IRaffleRule;
import com.wjy.marketcenter.service.strategy.IRaffleStock;
import com.wjy.marketcenter.service.strategy.rule.chain.ILogicChain;
import com.wjy.marketcenter.service.strategy.rule.chain.factory.DefaultChainFactory;
import com.wjy.marketcenter.service.strategy.rule.tree.factory.DefaultTreeFactory;
import com.wjy.marketcenter.service.strategy.rule.tree.factory.engine.IDecisionTreeEngine;
import com.wjy.marketcenter.valobj.RuleTreeVO;
import com.wjy.marketcenter.valobj.StrategyAwardRuleModelVO;
import com.wjy.marketcenter.valobj.StrategyAwardStockKeyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *  默认的抽奖策略实现
 */
@Slf4j
@Service
public class DefaultRaffleStrategy extends AbstractRaffleStrategy implements IRaffleStock, IRaffleAward, IRaffleRule {

    @Override
    public DefaultChainFactory.StrategyAwardVO raffleLogicChain(String userId, Long strategyId) {
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        return logicChain.logic(userId, strategyId);

    }

    /**
     * 1.根据策略id、奖品id，strategy_award表中查询后置规则树id，如果规则树id不存在，返回奖品
     * 2. 查询规则树信息
     *  2.1 根据treeId查rule_tree表，得到得到规则树的详细信息
     *  2.2 根据treeId查询rule_tree_node表，得到规则树下的所有结点
     *  2.3 根据treeId查rule_tree_node_line表，得到规则树中的所有边
     * 3. 组装树对象
     *  3.1. 把所有边组装到边map中，key为结点的起点的名字，value为起点相同的边的list
     *  3.2. 把所有点组装到点map中，key为结点的名字，value为结点对象。结点对象中由上一步边map中的list
     *  3.3. 组装树对象，树对象中，有根节点对象，和上一步的点map
     * 4. 创建规则树引擎，并执行
     *  4.1 次数锁处理器：判断用户抽奖次数，是否达到规定
     *  4.2 库存处理器：
     *      4.2.1. redissonClient.getAtomicLong(key).decrementAndGet();进行库存扣减，扣减完大于等于0，成功，否则失败
     *      4.2.2. 扣减成功对扣减成功后的库存加锁，兜底，防止超卖。
     *      4.2.3. 扣减库存成功，将库存扣减对象加入队列中（redis）。
     *      4.2.4. 定时任务根据活动id、策略id更新strategy_award表，award_count_surplus-1
     *  4.3 兜底奖励处理器：返回兜底奖励
     * @param userId     用户ID
     * @param strategyId 策略ID
     * @param awardId    奖品ID
     * @return
     */
    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId, Date endDateTime) {
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);
        if (null == strategyAwardRuleModelVO) {
            return DefaultTreeFactory.StrategyAwardVO.builder().awardId(awardId).build();
        }
        RuleTreeVO ruleTreeVO = repository.queryRuleTreeVOByTreeId(strategyAwardRuleModelVO.getRuleModels());
        if (null == ruleTreeVO) {
            throw new RuntimeException("存在抽奖策略配置的规则模型 Key，未在库表 rule_tree、rule_tree_node、rule_tree_line 配置对应的规则树信息 " + strategyAwardRuleModelVO.getRuleModels());
        }
        IDecisionTreeEngine treeEngine = defaultTreeFactory.openLogicTree(ruleTreeVO);
        return treeEngine.process(userId, strategyId, awardId, endDateTime);
    }

    @Override
    public DefaultTreeFactory.StrategyAwardVO raffleLogicTree(String userId, Long strategyId, Integer awardId) {
        return raffleLogicTree(userId, strategyId, awardId, null);
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

    /**
     * 1. 根据activityId查询策略id
     * 2. 根据策略id查询奖品信息
     * @param activityId 策略ID
     * @return
     */
    @Override
    public List<StrategyAwardEntity> queryRaffleStrategyAwardListByActivityId(Long activityId) {
        Long strategyId = repository.queryStrategyIdByActivityId(activityId);
        return queryRaffleStrategyAwardList(strategyId);
    }

    /**
     * 根据rule_key为"rule_lock"和tree_id in(treeIds)，查tule_tree_node表，得到rule_value
     * map中的key为treeId,value为ruleValue
     * @param treeIds 规则树ID值
     * @return
     */
    @Override
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        return repository.queryAwardRuleLockCount(treeIds);
    }


}
