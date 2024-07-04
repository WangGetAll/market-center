package com.wjy.marketcenter.service.rule.impl;
import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.RuleActionEntity;
import com.wjy.marketcenter.entity.RuleMatterEntity;
import com.wjy.marketcenter.repository.StrategyRepository;
import com.wjy.marketcenter.service.annotation.LogicStrategy;
import com.wjy.marketcenter.service.rule.ILogicFilter;
import com.wjy.marketcenter.service.rule.factory.DefaultLogicFactory;
import com.wjy.marketcenter.valobj.RuleLogicCheckTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 黑名单用户过滤规则，黑名单用户会有个默认的奖品
 */
@Slf4j
@Component
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.RULE_BLACKLIST)
public class RuleBlackListLogicFilter implements ILogicFilter<RuleActionEntity.RaffleBeforeEntity> {

    @Resource
    private StrategyRepository repository;

    @Override
    public RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> filter(RuleMatterEntity ruleMatterEntity) {
        log.info("规则过滤-黑名单 userId:{} strategyId:{} ruleModel:{}", ruleMatterEntity.getUserId(), ruleMatterEntity.getStrategyId(), ruleMatterEntity.getRuleModel());

        String userId = ruleMatterEntity.getUserId();

        // 查询规则详情
        // 黑名单规则的详情格式：奖品id:用户id,用户id,用户id
        String ruleValue = repository.queryStrategyRuleValue(ruleMatterEntity.getStrategyId(), ruleMatterEntity.getAwardId(), ruleMatterEntity.getRuleModel());
        String[] splitRuleValue = ruleValue.split(Constants.COLON);
        // 奖品id
        Integer awardId = Integer.parseInt(splitRuleValue[0]);
        // 黑名单用户id
        String[] userBlackIds = splitRuleValue[1].split(Constants.SPLIT);
        // 如果用户id在黑名单中
        for (String userBlackId : userBlackIds) {
            if (userId.equals(userBlackId)) {
                return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                        .ruleModel(DefaultLogicFactory.LogicModel.RULE_BLACKLIST.getCode())
                        .data(RuleActionEntity.RaffleBeforeEntity.builder()
                                .strategyId(ruleMatterEntity.getStrategyId())
                                .awardId(awardId)
                                .build())
                        .code(RuleLogicCheckTypeVO.TAKE_OVER.getCode())
                        .info(RuleLogicCheckTypeVO.TAKE_OVER.getInfo())
                        .build();
            }
        }

        return RuleActionEntity.<RuleActionEntity.RaffleBeforeEntity>builder()
                .code(RuleLogicCheckTypeVO.ALLOW.getCode())
                .info(RuleLogicCheckTypeVO.ALLOW.getInfo())
                .build();
    }

}
