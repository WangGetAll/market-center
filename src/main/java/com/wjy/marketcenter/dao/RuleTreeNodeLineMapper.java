package com.wjy.marketcenter.dao;

import com.wjy.marketcenter.po.RuleTreeNodeLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleTreeNodeLineMapper {
    /**
     * 根据treeid查rule_tree_node_line表，得到规则树中的所有边
     * @param treeId
     * @return
     */
    List<RuleTreeNodeLine> queryRuleTreeNodeLineListByTreeId(String treeId);
}
