package com.wjy.marketcenter.service.activity.rule;

/**
 * 下单规则过滤接口
 */
public interface IActionChainArmory {

    IActionChain next();

    IActionChain appendNext(IActionChain next);

}
