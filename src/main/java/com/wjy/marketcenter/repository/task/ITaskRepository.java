package com.wjy.marketcenter.repository.task;


import com.wjy.marketcenter.entity.task.TaskEntity;

import java.util.List;

/**
 *  任务服务仓储接口
 */
public interface ITaskRepository {

    List<TaskEntity> queryNoSendMessageTaskList();

    void sendMessage(TaskEntity taskEntity);

    void updateTaskSendMessageCompleted(String userId, String messageId);

    void updateTaskSendMessageFail(String userId, String messageId);

}
