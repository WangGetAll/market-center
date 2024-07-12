package com.wjy.marketcenter.dao.credit;

import cn.bugstack.middleware.db.router.annotation.DBRouterStrategy;
import com.wjy.marketcenter.po.credit.UserCreditOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分流水单 DAO
 */
@Mapper
@DBRouterStrategy(splitTable = true)
public interface IUserCreditOrderDao {

    void insert(UserCreditOrder userCreditOrderReq);

}
