package com.wjy.marketcenter.service.rule.factory;

import com.wjy.marketcenter.entity.RuleActionEntity;
import com.wjy.marketcenter.service.annotation.LogicStrategy;
import com.wjy.marketcenter.service.rule.ILogicFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.annotation.AnnotationUtils;
/**
 *  规则工厂
 */
@Service
public class DefaultLogicFactory {

    public Map<String, ILogicFilter<?>> logicFilterMap = new ConcurrentHashMap<>();

    public DefaultLogicFactory(List<ILogicFilter<?>> logicFilters) {
        logicFilters.forEach(filter -> {
            LogicStrategy strategy = AnnotationUtils.findAnnotation(filter.getClass(), LogicStrategy.class);
            if (null != strategy) {
                logicFilterMap.put(strategy.logicMode().getCode(), filter);
            }
        });
    }

    public <T extends RuleActionEntity.RaffleEntity> Map<String, ILogicFilter<T>> openLogicFilter() {
        return (Map<String, ILogicFilter<T>>) (Map<?, ?>) logicFilterMap;
    }

    @Getter
    @AllArgsConstructor
    public enum LogicModel {

        RULE_WIGHT("rule_weight", "【抽奖前规则】根据抽奖权重返回可抽奖范围KEY", "before"),
        RULE_BLACKLIST("rule_blacklist", "【抽奖前规则】黑名单规则过滤，命中黑名单则直接返回", "before"),
        RULE_LOCK("rule_lock", "【抽奖中规则】抽奖n次后，对应奖品可解锁抽奖", "center"),
        RULE_LUCK_AWARD("rule_luck_award", "【抽奖后规则】幸运奖兜底", "after"),
        ;


        private final String code;
        private final String info;
        private final String type;

        /**
         * 根据rule_weight、rule_blacklist等规则的名字判断其是否是抽奖中
         * @param code
         * @return
         */
        public static boolean isCenter(String code){
            return "center".equals(LogicModel.valueOf(code.toUpperCase()).type);
        }
        /**
         * 根据rule_weight、rule_blacklist等规则的名字判断其是否是抽奖后
         * @param code
         * @return
         */
        public static boolean isAfter(String code){
            return "after".equals(LogicModel.valueOf(code.toUpperCase()).type);
        }
    }
}
