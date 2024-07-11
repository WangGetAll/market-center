package com.wjy.marketcenter.mapper;

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
}
