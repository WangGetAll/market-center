package com.wjy.marketcenter.mapper.activity;
import com.wjy.marketcenter.po.activity.RaffleActivityCount;
import org.apache.ibatis.annotations.Mapper;
/**
 *  抽奖活动次数配置表Dao
 */
@Mapper
public interface IRaffleActivityCountDao {
    RaffleActivityCount queryRaffleActivityCountByActivityCountId(Long activityCountId);
}
