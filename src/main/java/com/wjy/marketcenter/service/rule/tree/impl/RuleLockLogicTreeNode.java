package com.wjy.marketcenter.service.rule.tree.impl;

import com.wjy.marketcenter.service.rule.tree.ILogicTreeNode;
import com.wjy.marketcenter.service.rule.tree.factory.DefaultTreeFactory;
import com.wjy.marketcenter.valobj.RuleLogicCheckTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 次数锁节点
 */
@Slf4j
@Component("rule_lock")
public class RuleLockLogicTreeNode implements ILogicTreeNode {

    @Override
    public DefaultTreeFactory.TreeActionEntity logic(String userId, Long strategyId, Integer awardId) {
        return DefaultTreeFactory.TreeActionEntity.builder()
                .ruleLogicCheckType(RuleLogicCheckTypeVO.ALLOW)
                .build();
    }

}
