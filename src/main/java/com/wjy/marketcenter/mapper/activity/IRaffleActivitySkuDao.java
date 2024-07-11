package com.wjy.marketcenter.mapper.activity;

import com.wjy.marketcenter.po.activity.RaffleActivitySku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品sku dao
 */

@Mapper
public interface IRaffleActivitySkuDao {

    RaffleActivitySku queryActivitySku(Long sku);
    void updateActivitySkuStock(Long sku);

    void clearActivitySkuStock(Long sku);

    /**
     * 根据activityId查询raffle_activity_sku表。key是sku，value是sku的库存
     * @param activityId
     * @return
     */
    List<RaffleActivitySku> queryActivitySkuListByActivityId(Long activityId);
}

