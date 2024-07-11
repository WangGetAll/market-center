package com.wjy.marketcenter.service.activity;

import com.wjy.marketcenter.entity.activity.PartakeRaffleActivityEntity;
import com.wjy.marketcenter.entity.activity.UserRaffleOrderEntity;

/**
 *  参与抽奖活务接口
 */
public interface IRaffleActivityPartakeService {

    /**
     *
     * @param userId     用户ID
     * @param activityId 活动ID
     * @return 用户抽奖订单实体对象
     */
    UserRaffleOrderEntity createOrder(String userId, Long activityId);


    /**
     * 1.根据userId、activityId查询用户的总、月、日抽奖次数，对总剩余抽奖次数校验
     * 2.根据userId、activityId、month查询用户的月抽奖次数
     *  2.1.如果有，对月抽奖次数校验
     *  2.2.如果没有，创建月抽奖次数对象
     * 3.根据userId、activityId、day查询用户的日抽奖次数
     *   3.1.如果有，对日抽奖次数校验
     *   3.2.如果没有，创建日抽奖次数对象
     * 4.由总、月、日抽奖次数、月抽奖次数、日抽奖次数、用户id、活动id构建创建抽奖单聚合对象
     * 5.根据userid查询活动信息，构建抽奖单对象（活动信息、用户id、订单号）
     * 6.抽奖单对象加入聚合对象中
     * 7.保存聚合对象
     *  7.1. 根据userId、activityId、总剩余抽奖次数大于0更新raffle_activity_account表，将总剩余抽奖次数都减一。
     *  7.2. 更新或新建月次数记录
     *   7.3. 存在月次数记录。
     *       7.3.1. 根据userId、activityId、month、month_count_surplus>0更新raffle_activity_account_month表，将表中的month_count_surplus减一
     *       7.3.2  根据根据userId、activityId、month_count_surplus>0更新raffle_activity_account表，将表中的month_count_surplus减一
     *   7.4. 不存在月次数记录。
     *       7.4.1. 新建月次数记录，月总次数为总次数表中的月总次数，月剩余次为总次数表中的月剩余次数减一。
     *       7.4.2. 根据userId、activityId更新raffle_activity_account表，将月剩余次数更新为之前查出来的月剩余次数减一。
     *  8. 更新或新建日次数记录（同月次数处理逻辑）
     *  9. 新建抽奖单记录
     * @param partakeRaffleActivityEntity 参与抽奖活动实体对象
     * @return 用户抽奖订单实体对象
     */
    UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity);

}
