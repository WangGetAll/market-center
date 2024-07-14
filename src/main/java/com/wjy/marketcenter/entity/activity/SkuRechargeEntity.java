package com.wjy.marketcenter.entity.activity;

import com.wjy.marketcenter.valobj.activity.OrderTradeTypeVO;
import lombok.Data;

/**
 *  活动商品充值实体对象
 */
@Data
public class SkuRechargeEntity {

    /**
     * 用户ID
     */
    private String userId;
    /**
     * 商品SKU - activity + activity count
     */
    private Long sku;
    /**
     * 幂等业务单号，外部谁充值谁透传，这样来保证幂等（多次调用也能确保结果唯一，不会多次充值）。
     */
    private String outBusinessNo;

    /** 用户ID */
    private OrderTradeTypeVO orderTradeType = OrderTradeTypeVO.rebate_no_pay_trade;

}
