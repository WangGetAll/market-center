package com.wjy.marketcenter.dao.award;

import com.wjy.marketcenter.po.award.UserCreditAccount;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分账户
 */
@Mapper
public interface IUserCreditAccountDao {

    /**
     * 向user_credit_account表中，增加一条记录
     * @param userCreditAccountReq
     */
    void insert(UserCreditAccount userCreditAccountReq);

    /**
     * 根据userid更新向user_credit_account表，增加total_amount和available_amount
     * @param userCreditAccountReq
     * @return
     */
    int updateAddAmount(UserCreditAccount userCreditAccountReq);

    UserCreditAccount queryUserCreditAccount(UserCreditAccount userCreditAccountReq);

    int updateSubtractionAmount(UserCreditAccount userCreditAccountReq);


}
