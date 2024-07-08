package com.wjy.marketcenter.aggregate;

import com.wjy.marketcenter.entity.activity.ActivityAccountEntity;
import com.wjy.marketcenter.entity.activity.ActivityOrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  下单聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 增加；总次数
     */
    private Integer totalCount;

    /**
     * 增加；日次数
     */
    private Integer dayCount;

    /**
     * 增加；月次数
     */
    private Integer monthCount;

    /**
     * 活动订单实体
     */
    private ActivityOrderEntity activityOrderEntity;

}

