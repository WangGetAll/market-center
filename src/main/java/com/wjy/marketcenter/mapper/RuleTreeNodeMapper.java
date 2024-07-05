package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.RuleTreeNode;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RuleTreeNodeMapper {
    List<RuleTreeNode> queryRuleTreeNodeListByTreeId(String treeId);
}
