package com.wjy.marketcenter.aggregate.award;
import com.wjy.marketcenter.entity.award.TaskEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  用户中奖记录聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAwardRecordAggregate {

    private UserAwardRecordEntity userAwardRecordEntity;

    private TaskEntity taskEntity;

}
