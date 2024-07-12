package com.wjy.marketcenter.mapper.rebate;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.wjy.marketcenter.po.rebate.UserBehaviorRebateOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户行为返利记录 Dao
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserBehaviorRebateOrderDao {

    void insert(UserBehaviorRebateOrder userBehaviorRebateOrder);

}
