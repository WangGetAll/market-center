package com.wjy.marketcenter.aggregate.rebate;

import com.wjy.marketcenter.entity.rebate.BehaviorRebateOrderEntity;
import com.wjy.marketcenter.entity.rebate.TaskEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为返利聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorRebateAggregate {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 行为返利订单实体对象
     */
    private BehaviorRebateOrderEntity behaviorRebateOrderEntity;
    /**
     * 任务实体对象
     */
    private TaskEntity taskEntity;

}
