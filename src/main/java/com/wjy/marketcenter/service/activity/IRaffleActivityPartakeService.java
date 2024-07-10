package com.wjy.marketcenter.service.activity;

import com.wjy.marketcenter.entity.activity.PartakeRaffleActivityEntity;
import com.wjy.marketcenter.entity.activity.UserRaffleOrderEntity;

/**
 *  参与抽奖活务接口
 */
public interface IRaffleActivityPartakeService {

    /**
     * 创建抽奖单；用户参与抽奖活动，扣减活动账户库存，产生抽奖单。如存在未被使用的抽奖单则直接返回已存在的抽奖单。
     * 用户参与抽奖生成抽奖单
     * @param partakeRaffleActivityEntity 参与抽奖活动实体对象
     * @return 用户抽奖订单实体对象
     */
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

}
