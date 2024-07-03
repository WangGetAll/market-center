package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyAwardMapper {
    List<StrategyAward> queryStrategyAwardList();

    /**
     *  根据策略id，查询该策略下，奖品id、奖品总库存、剩余库存、中奖概率
     * @param strategyId
     */
    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);
}
