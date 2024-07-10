package com.wjy.marketcenter.entity.task;

import com.wjy.marketcenter.event.BaseEvent;
import com.wjy.marketcenter.event.award.SendAwardMessageEvent;
import com.wjy.marketcenter.valobj.award.TaskStateVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务实体对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskEntity {

    /** 活动ID */
    private String userId;
    /** 消息主题 */
    private String topic;
    /** 消息编号 */
    private String messageId;
    /** 消息主体 */
    private String message;

}
