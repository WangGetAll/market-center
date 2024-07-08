package com.wjy.marketcenter.service.activity;

import com.wjy.marketcenter.repository.activity.IActivityRepository;
import org.springframework.stereotype.Service;

/**
 * 抽奖活动服务
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity {

    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

}
