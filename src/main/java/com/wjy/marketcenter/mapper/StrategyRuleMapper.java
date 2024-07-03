package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.StrategyRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyRuleMapper {
    List<StrategyRule> queryStrategyRuleList();
}
