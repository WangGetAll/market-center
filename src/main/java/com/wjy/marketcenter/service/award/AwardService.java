package com.wjy.marketcenter.service.award;

import com.wjy.marketcenter.aggregate.award.UserAwardRecordAggregate;
import com.wjy.marketcenter.entity.award.DistributeAwardEntity;
import com.wjy.marketcenter.entity.award.TaskEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;
import com.wjy.marketcenter.event.BaseEvent;
import com.wjy.marketcenter.event.award.SendAwardMessageEvent;
import com.wjy.marketcenter.repository.award.IAwardRepository;
import com.wjy.marketcenter.service.distribute.IDistributeAward;
import com.wjy.marketcenter.valobj.award.TaskStateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 奖品服务
 */
@Service
@Slf4j
public class AwardService implements IAwardService {



    private final IAwardRepository awardRepository;
    private final SendAwardMessageEvent sendAwardMessageEvent;
    private final Map<String, IDistributeAward> distributeAwardMap;

    public AwardService(IAwardRepository awardRepository, SendAwardMessageEvent sendAwardMessageEvent, Map<String, IDistributeAward> distributeAwardMap) {
        this.awardRepository = awardRepository;
        this.sendAwardMessageEvent = sendAwardMessageEvent;
        this.distributeAwardMap = distributeAwardMap;
    }


    /**
     * 1. 构建中奖事件消息对象
     * 2. 构建中奖任务对象
     * 3. 构建聚合对象（用户中奖对象，中奖任务对象），落库
     *   3.1. user_award_record表中新增记录
     *   3.2. task表中新增记录，状态为created
     *   3.3  更新抽奖单状态为used
     *   3.3. 发送中奖MQ
     *   3.4. 更新task记录，状态变更为completed
     *   3.5. 如果发送MQ失败，更新task记录，状态变更为fail
     *   3.6. 定时任务，扫描task表，重新执行3，4步操作
     * @param userAwardRecordEntity
     */
    @Override
    public void saveUserAwardRecord(UserAwardRecordEntity userAwardRecordEntity) {
        // 构建消息对象
        SendAwardMessageEvent.SendAwardMessage sendAwardMessage = new SendAwardMessageEvent.SendAwardMessage();
        sendAwardMessage.setUserId(userAwardRecordEntity.getUserId());
        sendAwardMessage.setOrderId(userAwardRecordEntity.getOrderId());
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());
        sendAwardMessage.setAwardConfig(userAwardRecordEntity.getAwardConfig());
        BaseEvent.EventMessage<SendAwardMessageEvent.SendAwardMessage> sendAwardMessageEventMessage = sendAwardMessageEvent.buildEventMessage(sendAwardMessage);

        // 构建任务对象
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(userAwardRecordEntity.getUserId());
        taskEntity.setTopic(sendAwardMessageEvent.topic());
        taskEntity.setMessageId(sendAwardMessageEventMessage.getId());
        taskEntity.setMessage(sendAwardMessageEventMessage);
        taskEntity.setState(TaskStateVO.create);

        // 构建聚合对象
        UserAwardRecordAggregate userAwardRecordAggregate = UserAwardRecordAggregate.builder()
                .taskEntity(taskEntity)
                .userAwardRecordEntity(userAwardRecordEntity)
                .build();

        // 存储聚合对象 - 一个事务下，用户的中奖记录
        awardRepository.saveUserAwardRecord(userAwardRecordAggregate);
    }

    /**
     * 1. 根据awardId查询award表，得到awardKey
     * 2. 根据awardKey拿到发奖处理器
     * 3. 发奖处理器发奖
     *  3.1. 随机积分发奖处理器
     *          根据awardId查询award表得到awardConfig（随机积分的最小值和最大值）
     *          根据配置生成一个随机的积分
     *          构建聚合对象
     *          更新用户积分或者新建用户积分记录
     *          更新用户中奖记录，根据userId、orderId、award_state为create,更新user_award_record表，将award_state为create更新为completed
     *
     * @param distributeAwardEntity
     */
    @Override
    public void distributeAward(DistributeAwardEntity distributeAwardEntity) {
        // 根据awardId查询award表，得到awardKey
        String awardKey = awardRepository.queryAwardKey(distributeAwardEntity.getAwardId());
        if (null == awardKey) {
            log.error("分发奖品，奖品ID不存在。awardKey:{}", awardKey);
            return;
        }

        // 根据awardKey拿到发奖处理器
        IDistributeAward distributeAward = distributeAwardMap.get(awardKey);

        if (null == distributeAward) {
            log.error("分发奖品，对应的服务不存在。awardKey:{}", awardKey);
            throw new RuntimeException("分发奖品，奖品" + awardKey + "对应的服务不存在");
        }

        // 发放奖品
        distributeAward.giveOutPrizes(distributeAwardEntity);
    }


}
