package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.RuleTree;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleTreeMapper {
    RuleTree queryRuleTreeByTreeId(String treeId);
}
