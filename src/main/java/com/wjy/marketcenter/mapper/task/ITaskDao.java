package com.wjy.marketcenter.mapper.task;

import cn.bugstack.middleware.db.router.annotation.DBRouter;
import com.wjy.marketcenter.po.task.Task;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  任务表，发送MQ
 */
@Mapper
public interface ITaskDao {

    void insert(Task task);

    @DBRouter
    void updateTaskSendMessageCompleted(Task task);

    @DBRouter
    void updateTaskSendMessageFail(Task task);

    List<Task> queryNoSendMessageTaskList();

}
