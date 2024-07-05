package com.wjy.marketcenter.service;


import com.wjy.marketcenter.entity.RaffleAwardEntity;
import com.wjy.marketcenter.entity.RaffleFactorEntity;
import com.wjy.marketcenter.entity.RuleActionEntity;
import com.wjy.marketcenter.entity.StrategyEntity;
import com.wjy.marketcenter.enums.ResponseCode;
import com.wjy.marketcenter.exception.AppException;
import com.wjy.marketcenter.repository.StrategyRepository;
import com.wjy.marketcenter.service.IRaffleStrategy;
import com.wjy.marketcenter.service.armory.StrategyArmory;
import com.wjy.marketcenter.service.rule.chain.ILogicChain;
import com.wjy.marketcenter.service.rule.chain.factory.DefaultChainFactory;
import com.wjy.marketcenter.service.rule.filter.factory.DefaultLogicFactory;
import com.wjy.marketcenter.valobj.RuleLogicCheckTypeVO;
import com.wjy.marketcenter.valobj.StrategyAwardRuleModelVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

/**
 * 抽奖策略抽象类，定义抽奖的标准流程
 */
@Slf4j
public abstract class AbstractRaffleStrategy implements IRaffleStrategy {
    @Resource
    private StrategyRepository repository;

    @Resource
    private StrategyArmory strategyArmory;


    @Resource
    private DefaultChainFactory defaultChainFactory;


    /**
     * 执行抽奖
     * @param raffleFactorEntity 抽奖因子实体对象，根据入参信息计算抽奖结果
     * @return
     */
    @Override
    public RaffleAwardEntity performRaffle(RaffleFactorEntity raffleFactorEntity) {
        // 1. 参数校验
        String userId = raffleFactorEntity.getUserId();
        Long strategyId = raffleFactorEntity.getStrategyId();
        if (null == strategyId || StringUtils.isBlank(userId)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
        // 获取过滤器链条，可能会有黑名单、权重
        ILogicChain logicChain = defaultChainFactory.openLogicChain(strategyId);
        // 责任链抽奖
        Integer awardId = logicChain.logic(userId, strategyId);

        // 5. 查询奖品规则「抽奖中（拿到奖品ID时，过滤规则）、抽奖后（扣减完奖品库存后过滤，抽奖中拦截和无库存则走兜底）」
        StrategyAwardRuleModelVO strategyAwardRuleModelVO = repository.queryStrategyAwardRuleModelVO(strategyId, awardId);

        // 6. 抽奖中 - 规则过滤
        RuleActionEntity<RuleActionEntity.RaffleCenterEntity> ruleActionCenterEntity = this.doCheckRaffleCenterLogic(RaffleFactorEntity.builder()
                .userId(userId)
                .strategyId(strategyId)
                .awardId(awardId)
                .build(), strategyAwardRuleModelVO.raffleCenterRuleModelList());

        if (RuleLogicCheckTypeVO.TAKE_OVER.getCode().equals(ruleActionCenterEntity.getCode())){
            log.info("【临时日志】中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。");
            return RaffleAwardEntity.builder()
                    .awardDesc("中奖中规则拦截，通过抽奖后规则 rule_luck_award 走兜底奖励。")
                    .build();
        }




        return RaffleAwardEntity.builder()
                .awardId(awardId)
                .build();
    }

    protected abstract RuleActionEntity<RuleActionEntity.RaffleBeforeEntity> doCheckRaffleBeforeLogic(RaffleFactorEntity raffleFactorEntity, String... logics);
    protected abstract RuleActionEntity<RuleActionEntity.RaffleCenterEntity> doCheckRaffleCenterLogic(RaffleFactorEntity raffleFactorEntity, String... logics);

}
