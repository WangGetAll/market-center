package com.wjy.marketcenter.mapper.activity;
import com.wjy.marketcenter.po.activity.RaffleActivityAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 *  抽奖活动账户表Dao
 */
@Mapper
public interface IRaffleActivityAccountDao {
    void insert(RaffleActivityAccount raffleActivityAccount);

    int updateAccountQuota(RaffleActivityAccount raffleActivityAccount);

}
