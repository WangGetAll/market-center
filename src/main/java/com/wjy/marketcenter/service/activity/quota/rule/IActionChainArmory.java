package com.wjy.marketcenter.service.activity.quota.rule;

/**
 * 下单规则过滤接口
 */
public interface IActionChainArmory {

    IActionChain next();

    IActionChain appendNext(IActionChain next);

}
