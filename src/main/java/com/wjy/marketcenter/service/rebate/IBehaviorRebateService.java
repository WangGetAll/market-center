package com.wjy.marketcenter.service.rebate;


import com.wjy.marketcenter.entity.rebate.BehaviorEntity;

import java.util.List;

/**
 * 行为返利服务接口
 */
public interface IBehaviorRebateService {

    /**
     * 创建返利单
     *
     * @param behaviorEntity 行为实体对象
     * @return 订单ID
     */
    List<String> createOrder(BehaviorEntity behaviorEntity);

}
