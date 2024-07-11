package com.wjy.marketcenter.service.activity.partake;

import com.alibaba.fastjson.JSON;
import com.wjy.marketcenter.aggregate.CreatePartakeOrderAggregate;
import com.wjy.marketcenter.entity.activity.ActivityEntity;
import com.wjy.marketcenter.entity.activity.PartakeRaffleActivityEntity;
import com.wjy.marketcenter.entity.activity.UserRaffleOrderEntity;
import com.wjy.marketcenter.enums.ResponseCode;
import com.wjy.marketcenter.exception.AppException;
import com.wjy.marketcenter.repository.activity.IActivityRepository;
import com.wjy.marketcenter.service.activity.IRaffleActivityPartakeService;
import com.wjy.marketcenter.valobj.activity.ActivityStateVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 抽奖活动参与抽奖类
 */
@Slf4j
public abstract class AbstractRaffleActivityPartake implements IRaffleActivityPartakeService {

    protected final IActivityRepository activityRepository;

    public AbstractRaffleActivityPartake(IActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }


    @Override
    public UserRaffleOrderEntity createOrder(String userId, Long activityId) {
        return createOrder(PartakeRaffleActivityEntity.builder()
                .userId(userId)
                .activityId(activityId)
                .build());
    }

    @Override
    public UserRaffleOrderEntity createOrder(PartakeRaffleActivityEntity partakeRaffleActivityEntity) {
        // 0. 基础信息
        String userId = partakeRaffleActivityEntity.getUserId();
        Long activityId = partakeRaffleActivityEntity.getActivityId();
        Date currentDate = new Date();

        // 1. 活动查询
        ActivityEntity activityEntity = activityRepository.queryRaffleActivityByActivityId(activityId);

        // 校验；活动状态
        if (!ActivityStateVO.open.equals(activityEntity.getState())) {
            throw new AppException(ResponseCode.ACTIVITY_STATE_ERROR.getCode(), ResponseCode.ACTIVITY_STATE_ERROR.getInfo());
        }
        // 校验；活动日期「开始时间 <- 当前时间 -> 结束时间」
        if (activityEntity.getBeginDateTime().after(currentDate) || activityEntity.getEndDateTime().before(currentDate)) {
            throw new AppException(ResponseCode.ACTIVITY_DATE_ERROR.getCode(), ResponseCode.ACTIVITY_DATE_ERROR.getInfo());
        }

        // 2. 查询未被使用的活动参与订单记录
        UserRaffleOrderEntity userRaffleOrderEntity = activityRepository.queryNoUsedRaffleOrder(partakeRaffleActivityEntity);
        if (null != userRaffleOrderEntity) {
            log.info("创建参与活动订单 userId:{} activityId:{} userRaffleOrderEntity:{}", userId, activityId, JSON.toJSONString(userRaffleOrderEntity));
            return userRaffleOrderEntity;
        }

        // 3. 额度账户过滤&返回账户构建对象
        CreatePartakeOrderAggregate createPartakeOrderAggregate = this.doFilterAccount(userId, activityId, currentDate);

        // 4. 构建订单
        UserRaffleOrderEntity userRaffleOrder = this.buildUserRaffleOrder(userId, activityId, currentDate);

        // 5. 填充抽奖单实体对象
        createPartakeOrderAggregate.setUserRaffleOrderEntity(userRaffleOrder);

        // 6. 保存聚合对象 - 一个领域内的一个聚合是一个事务操作
        activityRepository.saveCreatePartakeOrderAggregate(createPartakeOrderAggregate);

        // 7. 返回订单信息
        return userRaffleOrder;
    }

    /**
     * 根据userId、activityId查询用户的总、月、日抽奖次数，对总剩余抽奖次数校验
     * 根据userId、activityId、month查询用户的月抽奖次数
     *  如果有，对月抽奖次数校验
     *  如果没有，创建月抽奖次数对象
     * 根据userId、activityId、day查询用户的日抽奖次数
     *   如果有，对日抽奖次数校验
     *   如果没有，创建日抽奖次数对象
     * 由总、月、日抽奖次数、月抽奖次数、日抽奖次数、用户id、活动id构建创建抽奖单聚合对象
     * @param userId
     * @param activityId
     * @param currentDate
     * @return
     */
    protected abstract CreatePartakeOrderAggregate doFilterAccount(String userId, Long activityId, Date currentDate);

    /**
     * 根据userid查询活动信息，构建抽奖单对象（活动信息、用户id、订单号）
     * @param userId
     * @param activityId
     * @param currentDate
     * @return
     */
    protected abstract UserRaffleOrderEntity buildUserRaffleOrder(String userId, Long activityId, Date currentDate);

}
