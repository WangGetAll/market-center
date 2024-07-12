package com.wjy.marketcenter.entity.rebate;

import com.wjy.marketcenter.valobj.rebate.BehaviorTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行为实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BehaviorEntity {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 行为类型；sign 签到、openai_pay 支付
     */
    private BehaviorTypeVO behaviorTypeVO;
    /**
     * 业务ID；签到则是日期字符串，支付则是外部的业务ID
     */
    private String outBusinessNo;

}
