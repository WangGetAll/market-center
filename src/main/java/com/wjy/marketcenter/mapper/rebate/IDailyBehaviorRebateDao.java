package com.wjy.marketcenter.mapper.rebate;

import com.wjy.marketcenter.po.rebate.DailyBehaviorRebate;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 日常行为返利配置 Dao
 */
@Mapper
public interface IDailyBehaviorRebateDao {

    List<DailyBehaviorRebate> queryDailyBehaviorRebateByBehaviorType(String behaviorType);

}
