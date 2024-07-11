package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.Strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyMapper {
    List<Strategy> queryStrategyList();

    /**
     * 根据strategyId查strategy表，获得该策略配置的规则
     * @param strategyId
     * @return
     */
    Strategy queryStrategyByStrategyId(Long strategyId);
}
