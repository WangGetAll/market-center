package com.wjy.marketcenter.dao.award;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.wjy.marketcenter.po.award.UserAwardRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户中奖记录表
 */

@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserAwardRecordDao {

    void insert(UserAwardRecord userAwardRecord);

    /**
     * 根据userId、orderId、award_state为create,更新user_award_record表，将award_state为create更新为completed
     * @param userAwardRecordReq
     * @return
     */
    int updateAwardRecordCompletedState(UserAwardRecord userAwardRecordReq);

}
