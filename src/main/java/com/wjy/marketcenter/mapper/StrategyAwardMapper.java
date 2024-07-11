package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyAwardMapper {
    List<StrategyAward> queryStrategyAwardList();

    /**
     *  根据策略id，查询strategy_award表，得到该策略下的奖品信息（奖品库存、中奖概率等）
     * @param strategyId
     */
    List<StrategyAward> queryStrategyAwardListByStrategyId(Long strategyId);

    /**
     * 根据策略id、奖品id查strategy_award表，得到为这个奖品配置的规则树id
     * @param strategyAward
     * @return
     */
    String queryStrategyAwardRuleModels(StrategyAward strategyAward);

    void updateStrategyAwardStock(StrategyAward strategyAward);

    StrategyAward queryStrategyAward(StrategyAward strategyAwardReq);
}
