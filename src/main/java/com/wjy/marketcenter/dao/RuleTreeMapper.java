package com.wjy.marketcenter.dao;

import com.wjy.marketcenter.po.RuleTree;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleTreeMapper {
    /**
     * 根据treeId查rule_tree表，得到得到规则树的详细信息
     * @param treeId
     * @return
     */
    RuleTree queryRuleTreeByTreeId(String treeId);
}
