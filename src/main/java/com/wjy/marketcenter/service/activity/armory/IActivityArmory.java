package com.wjy.marketcenter.service.activity.armory;

/**
 * 活动装配预热
 */
public interface IActivityArmory {

    boolean assembleActivitySku(Long sku);

    /**
     * 根据activityId查询活动sku信息，并缓存，key为sku，value为库存。
     * 根据suk信息中的activity_account_id查询活动参与次数增加规则【优先走缓存，未命中加载】。key为activityCountId，value为ActivityCountEntity对象
     * 根据activityId查询活动的信息。优先走缓存，未命中，写缓存。key为activityId，value为ActivityEntity。
     * @param activityId
     * @return
     */
    boolean assembleActivitySkuByActivityId(Long activityId);

}
