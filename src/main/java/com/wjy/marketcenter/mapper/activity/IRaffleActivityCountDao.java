package com.wjy.marketcenter.mapper.activity;
import com.wjy.marketcenter.po.activity.RaffleActivityCount;
import org.apache.ibatis.annotations.Mapper;
/**
 *  抽奖活动次数配置表Dao
 */
@Mapper
public interface IRaffleActivityCountDao {
    /**
     * 根据activityCountId查询次增长策略
     * @param activityCountId
     * @return
     */
    RaffleActivityCount queryRaffleActivityCountByActivityCountId(Long activityCountId);
}
