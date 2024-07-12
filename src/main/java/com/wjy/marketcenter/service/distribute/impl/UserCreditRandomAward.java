package com.wjy.marketcenter.service.distribute.impl;

import com.wjy.marketcenter.aggregate.award.GiveOutPrizesAggregate;
import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.award.DistributeAwardEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;
import com.wjy.marketcenter.entity.award.UserCreditAwardEntity;
import com.wjy.marketcenter.repository.award.IAwardRepository;
import com.wjy.marketcenter.service.distribute.IDistributeAward;
import com.wjy.marketcenter.valobj.award.AwardStateVO;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 用户积分奖品，支持 award_config 透传，满足黑名单积分奖励。
 */
@Component("user_credit_random")
public class UserCreditRandomAward implements IDistributeAward {

    @Resource
    private IAwardRepository repository;

    /**
     * 根据awardId查询award表得到awardConfig（随机积分的最小值和最大值）
     * 根据配置生成一个随机的积分
     * 构建聚合对象
     * 更新用户积分或者新建用户积分记录
     * 更新用户中奖记录，根据userId、orderId、award_state为create,更新user_award_record表，将award_state为create更新为completed
     *
     * @param distributeAwardEntity
     */
    @Override
    public void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) {
        // 奖品ID
        Integer awardId = distributeAwardEntity.getAwardId();
        // 查询奖品发放配置 「优先走透传的随机积分奖品配置」
        String awardConfig = distributeAwardEntity.getAwardConfig();
        if (StringUtils.isBlank(awardConfig)) {
            awardConfig = repository.queryAwardConfig(awardId);
        }

        String[] creditRange = awardConfig.split(Constants.SPLIT);
        if (creditRange.length != 2) {
            throw new RuntimeException("award_config 「" + awardConfig + "」配置不是一个范围值，如 1,100");
        }

        // 生成随机积分值
        BigDecimal creditAmount = generateRandom(new BigDecimal(creditRange[0]), new BigDecimal(creditRange[1]));

        // 构建聚合对象
        UserAwardRecordEntity userAwardRecordEntity = GiveOutPrizesAggregate.buildDistributeUserAwardRecordEntity(
                distributeAwardEntity.getUserId(),
                distributeAwardEntity.getOrderId(),
                distributeAwardEntity.getAwardId(),
                AwardStateVO.complete
        );

        UserCreditAwardEntity userCreditAwardEntity = GiveOutPrizesAggregate.buildUserCreditAwardEntity(
                distributeAwardEntity.getUserId(),
                creditAmount);

        GiveOutPrizesAggregate giveOutPrizesAggregate = new GiveOutPrizesAggregate();
        giveOutPrizesAggregate.setUserId(distributeAwardEntity.getUserId());
        giveOutPrizesAggregate.setUserAwardRecordEntity(userAwardRecordEntity);
        giveOutPrizesAggregate.setUserCreditAwardEntity(userCreditAwardEntity);

        // 存储发奖对象
        repository.saveGiveOutPrizesAggregate(giveOutPrizesAggregate);
    }

    private BigDecimal generateRandom(BigDecimal min, BigDecimal max) {
        if (min.equals(max)) return min;
        BigDecimal randomBigDecimal = min.add(BigDecimal.valueOf(Math.random()).multiply(max.subtract(min)));
        return randomBigDecimal.round(new MathContext(3));
    }

}

