package com.wjy.marketcenter.service.award;

import com.wjy.marketcenter.aggregate.award.UserAwardRecordAggregate;
import com.wjy.marketcenter.entity.award.TaskEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;
import com.wjy.marketcenter.event.BaseEvent;
import com.wjy.marketcenter.event.award.SendAwardMessageEvent;
import com.wjy.marketcenter.repository.award.IAwardRepository;
import com.wjy.marketcenter.valobj.award.TaskStateVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 奖品服务
 */
@Service
public class AwardService implements IAwardService {

    @Resource
    private IAwardRepository awardRepository;
    @Resource
    private SendAwardMessageEvent sendAwardMessageEvent;

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
        sendAwardMessage.setAwardId(userAwardRecordEntity.getAwardId());
        sendAwardMessage.setAwardTitle(userAwardRecordEntity.getAwardTitle());
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

}
