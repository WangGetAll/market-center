package com.wjy.marketcenter.repository.rebate;


import com.wjy.marketcenter.aggregate.rebate.BehaviorRebateAggregate;
import com.wjy.marketcenter.entity.rebate.BehaviorRebateOrderEntity;
import com.wjy.marketcenter.valobj.rebate.BehaviorTypeVO;
import com.wjy.marketcenter.valobj.rebate.DailyBehaviorRebateVO;

import java.util.List;

/**
 * 行为返利服务仓储接口
 */
public interface IBehaviorRebateRepository {

    List<DailyBehaviorRebateVO> queryDailyBehaviorRebateConfig(BehaviorTypeVO behaviorTypeVO);

    void saveUserRebateRecord(String userId, List<BehaviorRebateAggregate> behaviorRebateAggregates);

    List<BehaviorRebateOrderEntity> queryOrderByOutBusinessNo(String userId, String outBusinessNo);

}
