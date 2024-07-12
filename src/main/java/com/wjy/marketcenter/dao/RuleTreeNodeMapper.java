package com.wjy.marketcenter.dao;

import com.wjy.marketcenter.po.RuleTreeNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleTreeNodeMapper {
    /**
     * 根据treeId查询rule_tree_node表，得到规则树下的所有结点
     * @param treeId
     * @return
     */
    List<RuleTreeNode> queryRuleTreeNodeListByTreeId(String treeId);

    /**
     * 根据rule_key为"rule_lock"和tree_id in(treeIds)，查tule_tree_node表，得到rule_value
     * @param treeIds
     * @return
     */
    List<RuleTreeNode> queryRuleLocks(String[] treeIds);
}
