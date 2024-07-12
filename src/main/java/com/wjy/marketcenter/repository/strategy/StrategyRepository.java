package com.wjy.marketcenter.repository.strategy;

import javax.annotation.Resource;

import com.wjy.marketcenter.common.Constants;
import com.wjy.marketcenter.entity.StrategyAwardEntity;
import com.wjy.marketcenter.entity.StrategyEntity;
import com.wjy.marketcenter.entity.StrategyRuleEntity;
import com.wjy.marketcenter.exception.AppException;
import com.wjy.marketcenter.dao.*;
import com.wjy.marketcenter.dao.activity.IRaffleActivityAccountDao;
import com.wjy.marketcenter.dao.activity.IRaffleActivityAccountDayDao;
import com.wjy.marketcenter.dao.activity.IRaffleActivityDao;
import com.wjy.marketcenter.po.*;
import com.wjy.marketcenter.po.activity.RaffleActivityAccount;
import com.wjy.marketcenter.po.activity.RaffleActivityAccountDay;
import com.wjy.marketcenter.redis.RedisService;
import com.wjy.marketcenter.service.strategy.rule.chain.factory.DefaultChainFactory;
import com.wjy.marketcenter.valobj.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.wjy.marketcenter.enums.ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY;

@Slf4j
@Repository
public class StrategyRepository  {
    @Resource
    private IRaffleActivityDao raffleActivityDao;
    @Resource
    private IRaffleActivityAccountDayDao raffleActivityAccountDayDao;

    @Resource
    private StrategyAwardMapper strategyAwardMapper;
    @Resource
    private StrategyRuleMapper strategyRuleMapper;

    @Resource
    private StrategyMapper strategyMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private RuleTreeMapper ruleTreeMapper;

    @Resource
    private RuleTreeNodeLineMapper ruleTreeNodeLineMapper;

    @Resource
    private RuleTreeNodeMapper ruleTreeNodeMapper;

    @Resource
    private IRaffleActivityAccountDao raffleActivityAccountDao;



    /**
     * 根据startegyId，查询该策略下有哪些奖品，每种奖品的库存、剩余库存、中奖概率
     * @param strategyId
     * @return
     */
    public List<StrategyAwardEntity> queryStrategyAwardList(Long strategyId) {
        // 优先从redis中获取，策略对应的奖品信息
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntities = redisService.getValue(cacheKey);
        if (null != strategyAwardEntities && !strategyAwardEntities.isEmpty()) return strategyAwardEntities;
        // 缓存中没有，从mysql中获取策略对应的奖品信息
        List<StrategyAward> strategyAwards = strategyAwardMapper.queryStrategyAwardListByStrategyId(strategyId);
        strategyAwardEntities = new ArrayList<>(strategyAwards.size());
        for (StrategyAward strategyAward : strategyAwards) {
            StrategyAwardEntity strategyAwardEntity = StrategyAwardEntity.builder()
                    .strategyId(strategyAward.getStrategyId())
                    .awardId(strategyAward.getAwardId())
                    .awardTitle(strategyAward.getAwardTitle())
                    .awardSubtitle(strategyAward.getAwardSubtitle())
                    .awardCount(strategyAward.getAwardCount())
                    .awardCountSurplus(strategyAward.getAwardCountSurplus())
                    .awardRate(strategyAward.getAwardRate())
                    .sort(strategyAward.getSort())
                    .ruleModels(strategyAward.getRuleModels())
                    .build();
            strategyAwardEntities.add(strategyAwardEntity);
        }
        // 加載到緩存
        redisService.setValue(cacheKey, strategyAwardEntities);
        return strategyAwardEntities;
    }

    /**
     * 缓存该策略下抽奖时的生成随机数的最大值。key为策略id，value为最大值
     * 缓存该策略下生成的随机数对应的奖品id。key为策略id，filed为生成的随机数，value为奖品id
     * @param key 策略id
     * @param rateRange 抽奖时生成的随机值的最大值
     * @param strategyAwardSearchRateTable map，key为抽奖时生成的随机值，value为奖品id
     */
    public void storeStrategyAwardSearchRateTable(String key, Integer rateRange, Map<Integer, Integer> strategyAwardSearchRateTable) {
        // 1. 存储抽奖策略范围值，如10000，用于生成1000以内的随机数
        // key:strategyId, value:当前策略对应的范围
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange);
        // 2. 存储概率查找表
        // key:strategyId，value: hash(key:随机数，value:奖品id)
        Map<Integer, Integer> cacheRateTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        cacheRateTable.putAll(strategyAwardSearchRateTable);
    }

    /**
     * 根据策略id和生成的随机数，从redis中获取奖品的id。
     * @param strategyId
     * @param rateKey
     * @return
     */
    public Integer getStrategyAwardAssemble(String strategyId, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + strategyId, rateKey);
    }

    /**
     *  根据策略id获取策略对应的抽奖范围
     * @param strategyId
     * @return
     */
    public int getRateRange(Long strategyId) {
        return getRateRange(String.valueOf(strategyId));
    }

    /**
     *  根据策略id+权重规则获取策略对应的抽奖范围
     * @param key
     * @return
     */
    public int getRateRange(String key) {
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(cacheKey);
    }


    /**
     * 根据策略id，查询策略信息（策略名称、策略配置的规则）【优先走缓存，未命中加载】
     * @param strategyId
     * @return
     */
    public StrategyEntity queryStrategyEntityByStrategyId(Long strategyId) {
        // 查redis
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if (null != strategyEntity) return strategyEntity;
        // 查mysql
        Strategy strategy = strategyMapper.queryStrategyByStrategyId(strategyId);
        strategyEntity = StrategyEntity.builder()
                .strategyId(strategy.getStrategyId())
                .strategyDesc(strategy.getStrategyDesc())
                .ruleModels(strategy.getRuleModels())
                .build();
        // 存redis
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }


    /**
     * 根据策略id和规则，查询规则的具体信息
     * @param strategyId
     * @param ruleModel
     * @return
     */
    public StrategyRuleEntity queryStrategyRule(Long strategyId, String ruleModel) {
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(ruleModel);
        StrategyRule strategyRuleRes = strategyRuleMapper.queryStrategyRule(strategyRuleReq);
        return StrategyRuleEntity.builder()
                .strategyId(strategyRuleRes.getStrategyId())
                .awardId(strategyRuleRes.getAwardId())
                .ruleType(strategyRuleRes.getRuleType())
                .ruleModel(strategyRuleRes.getRuleModel())
                .ruleValue(strategyRuleRes.getRuleValue())
                .ruleDesc(strategyRuleRes.getRuleDesc())
                .build();
    }

    /**
     * 根据策略id、奖品id（如果有）、规则类型，strategy_rule表中去查询规则的详情
     * @param strategyId
     * @param awardId
     * @param ruleModel
     * @return
     */
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule strategyRule = new StrategyRule();
        strategyRule.setStrategyId(strategyId);
        strategyRule.setAwardId(awardId);
        strategyRule.setRuleModel(ruleModel);
        return strategyRuleMapper.queryStrategyRuleValue(strategyRule);
    }

    /**
     * 根据策略id、规则类型，strategy_rule表中去查询规则的详情
     * @param strategyId
     * @param ruleModel
     * @return
     */
    public String queryStrategyRuleValue(Long strategyId, String ruleModel) {
       return queryStrategyRuleValue(strategyId, null, ruleModel);
    }

    /**
     * 根据策略id、奖品id查询strategy_award表，得到规则树id
     * @param awardId
     * @return
     */
    public StrategyAwardRuleModelVO queryStrategyAwardRuleModelVO(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        String ruleModels = strategyAwardMapper.queryStrategyAwardRuleModels(strategyAward);
        if (null == ruleModels) return null;
        return StrategyAwardRuleModelVO.builder().ruleModels(ruleModels).build();

    }

    /**
     * 1. 查询规则树信息
     *  1.1 根据treeId查rule_tree表，得到得到规则树的详细信息
     *  1.2 根据treeId查询rule_tree_node表，得到规则树下的所有结点
     *  1.3 根据treeId查rule_tree_node_line表，得到规则树中的所有边
     * 2. 组装树对象
     *  2.1. 把所有边组装到边map中，key为结点的起点的名字，value为起点相同的边的list
     *  2.2. 把所有点组装到点map中，key为结点的名字，value为结点对象。结点对象中由上一步边map中的list
     *  2.3. 组装树对象，树对象中，有根节点对象，和上一步的点map
     * @param treeId
     * @return
     */
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVOCache = redisService.getValue(cacheKey);
        if (null != ruleTreeVOCache) return ruleTreeVOCache;

        // 从数据库获取
        RuleTree ruleTree = ruleTreeMapper.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeMapper.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLines = ruleTreeNodeLineMapper.queryRuleTreeNodeLineListByTreeId(treeId);

        // 1. tree node line 转换Map结构
        // key:起始结点的名字，value：所有起始结点一样的边对象的list
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for (RuleTreeNodeLine ruleTreeNodeLine : ruleTreeNodeLines) {
            // ruleTreeNodeLine -> ruleTreeNodeLineVO
            RuleTreeNodeLineVO ruleTreeNodeLineVO = RuleTreeNodeLineVO.builder()
                    .treeId(ruleTreeNodeLine.getTreeId())
                    .ruleNodeFrom(ruleTreeNodeLine.getRuleNodeFrom())
                    .ruleNodeTo(ruleTreeNodeLine.getRuleNodeTo())
                    .ruleLimitType(RuleLimitTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitType()))
                    .ruleLimitValue(RuleLogicCheckTypeVO.valueOf(ruleTreeNodeLine.getRuleLimitValue()))
                    .build();
            // 有就获取List，没有就新建
            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOList = ruleTreeNodeLineMap.computeIfAbsent(ruleTreeNodeLine.getRuleNodeFrom(), k -> new ArrayList<>());
            // 加入List
            ruleTreeNodeLineVOList.add(ruleTreeNodeLineVO);
        }

        // 2. tree node 转换为Map结构
        // key:结点的名字 value:结点
        Map<String, RuleTreeNodeVO> treeNodeMap = new HashMap<>();
        for (RuleTreeNode ruleTreeNode : ruleTreeNodes) {
            // ruleTreeNode -> ruleTreeNodeVO
            RuleTreeNodeVO ruleTreeNodeVO = RuleTreeNodeVO.builder()
                    .treeId(ruleTreeNode.getTreeId())
                    .ruleKey(ruleTreeNode.getRuleKey())
                    .ruleDesc(ruleTreeNode.getRuleDesc())
                    .ruleValue(ruleTreeNode.getRuleValue())
                    .treeNodeLineVOList(ruleTreeNodeLineMap.get(ruleTreeNode.getRuleKey()))
                    .build();
            treeNodeMap.put(ruleTreeNode.getRuleKey(), ruleTreeNodeVO);
        }

        // 3. 构建 Rule Tree

        RuleTreeVO ruleTreeVODB = RuleTreeVO.builder()
                .treeId(ruleTree.getTreeId())
                .treeName(ruleTree.getTreeName())
                .treeDesc(ruleTree.getTreeDesc())
                .treeRootRuleNode(ruleTree.getTreeRootRuleKey())
                .treeNodeMap(treeNodeMap)
                .build();

        redisService.setValue(cacheKey, ruleTreeVODB);
        return ruleTreeVODB;
    }

    /**
     *  缓存奖品库存
     * @param cacheKey
     * @return
     */
    public void cacheStrategyAwardCount(String cacheKey, Integer awardCount) {
        if (null != redisService.getValue(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }


    public Boolean subtractionAwardStock(String cacheKey) {
        return subtractionAwardStock(cacheKey, null);
    }
    /**
     *  根据策略id和奖品id。扣减奖品缓存里的库存
     * @param cacheKey
     * @return
     */
    public Boolean subtractionAwardStock(String cacheKey, Date endDateTime) {
        long surplus = redisService.decr(cacheKey);
        if (surplus < 0) {
            // 库存小于0，恢复为0个
            redisService.setAtomicLong(cacheKey, 0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了。
        String lockKey = cacheKey + Constants.UNDERLINE + surplus;
        Boolean lock = false;
        if (null != endDateTime) {
            long expireMillis = endDateTime.getTime() - System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
            lock = redisService.setNx(lockKey, expireMillis, TimeUnit.MILLISECONDS);
        } else {
            lock = redisService.setNx(lockKey);
        }
        if (!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }



    /**
     * 消息写入队列
     * @param strategyAwardStockKeyVO
     */
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    /**
     *  从队列中，获取一个值
     * @return
     * @throws InterruptedException
     */
    public StrategyAwardStockKeyVO takeQueueValue() throws InterruptedException {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> destinationQueue = redisService.getBlockingQueue(cacheKey);
        return destinationQueue.poll();
    }

    /**
     *  更新数据库
     * @param strategyId
     * @param awardId
     */
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setStrategyId(strategyId);
        strategyAward.setAwardId(awardId);
        strategyAwardMapper.updateStrategyAwardStock(strategyAward);
    }


    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + Constants.UNDERLINE + awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if (null != strategyAwardEntity) return strategyAwardEntity;
        // 查询数据
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);
        StrategyAward strategyAwardRes = strategyAwardMapper.queryStrategyAward(strategyAwardReq);
        // 转换数据
        strategyAwardEntity = StrategyAwardEntity.builder()
                .strategyId(strategyAwardRes.getStrategyId())
                .awardId(strategyAwardRes.getAwardId())
                .awardTitle(strategyAwardRes.getAwardTitle())
                .awardSubtitle(strategyAwardRes.getAwardSubtitle())
                .awardCount(strategyAwardRes.getAwardCount())
                .awardCountSurplus(strategyAwardRes.getAwardCountSurplus())
                .awardRate(strategyAwardRes.getAwardRate())
                .sort(strategyAwardRes.getSort())
                .build();
        // 缓存结果
        redisService.setValue(cacheKey, strategyAwardEntity);
        // 返回数据
        return strategyAwardEntity;
    }


    /**
     * 根据activityId查询策略id
     * @param activityId
     * @return
     */
    public Long queryStrategyIdByActivityId(Long activityId) {
        return raffleActivityDao.queryStrategyIdByActivityId(activityId);
    }

    /**
     * 根据策略id，查raffle_activity表，得到活动id
     * 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数
     * 总次数减剩余次数得到用户当天的抽奖次数
     * @param userId
     * @param strategyId
     * @return
     */
    public Integer queryTodayUserRaffleCount(String userId, Long strategyId) {
        // 活动ID
        Long activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
        // 封装参数
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(raffleActivityAccountDayReq.currentDay());
        RaffleActivityAccountDay raffleActivityAccountDay = raffleActivityAccountDayDao.queryActivityAccountDayByUserId(raffleActivityAccountDayReq);
        if (null == raffleActivityAccountDay) return 0;
        // 总次数 - 剩余的，等于今日参与的
        return raffleActivityAccountDay.getDayCount() - raffleActivityAccountDay.getDayCountSurplus();
    }

    /**
     * 根据rule_key为"rule_lock"和tree_id in(treeIds)，查tule_tree_node表，得到rule_value
     * map中的key为treeId,value为ruleValue
     * @param treeIds
     * @return
     */
    public Map<String, Integer> queryAwardRuleLockCount(String[] treeIds) {
        if (null == treeIds || treeIds.length == 0) return new HashMap<>();
        List<RuleTreeNode> ruleTreeNodes = ruleTreeNodeMapper.queryRuleLocks(treeIds);
        Map<String, Integer> resultMap = new HashMap<>();
        for (RuleTreeNode node : ruleTreeNodes) {
            String treeId = node.getTreeId();
            Integer ruleValue = Integer.valueOf(node.getRuleValue());
            resultMap.put(treeId, ruleValue);
        }
        return resultMap;
    }


    public Integer queryActivityAccountTotalUseCount(String userId, Long strategyId) {
        Long activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
        RaffleActivityAccount raffleActivityAccount = raffleActivityAccountDao.queryActivityAccountByUserId(RaffleActivityAccount.builder()
                .userId(userId)
                .activityId(activityId)
                .build());
        // 返回计算使用量
        return raffleActivityAccount.getTotalCount() - raffleActivityAccount.getTotalCountSurplus();
    }

    /**
     * 1. 根据策略id、规则名称为"rule_weight"查询strategy_rule表，得到权重规则具体配置
     * 2. 解析配置，
     * 3. 把根据配置中过奖品id查询strategy_award表，得到奖品的标题
     * 4. 组装得到权重对象列表。每个权重对象中都有权重值、奖品列表两个属性
     * @param strategyId
     * @return
     */
    public List<RuleWeightVO> queryAwardRuleWeight(Long strategyId) {
        // 优先从缓存获取
        String cacheKey = Constants.RedisKey.STRATEGY_RULE_WEIGHT_KEY + strategyId;
        List<RuleWeightVO> ruleWeightVOS = redisService.getValue(cacheKey);
        if (null != ruleWeightVOS) return ruleWeightVOS;

        ruleWeightVOS = new ArrayList<>();
        // 1. 查询权重规则配置
        StrategyRule strategyRuleReq = new StrategyRule();
        strategyRuleReq.setStrategyId(strategyId);
        strategyRuleReq.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        String ruleValue = strategyRuleMapper.queryStrategyRuleValue(strategyRuleReq);
        // 2. 借助实体对象转换规则
        StrategyRuleEntity strategyRuleEntity = new StrategyRuleEntity();
        strategyRuleEntity.setRuleModel(DefaultChainFactory.LogicModel.RULE_WEIGHT.getCode());
        strategyRuleEntity.setRuleValue(ruleValue);
        Map<String, List<Integer>> ruleWeightValues = strategyRuleEntity.getRuleWeightValues();
        // 3. 遍历规则组装奖品配置
        for (String ruleWeightKey : ruleWeightValues.keySet()) {
            List<Integer> awardIds = ruleWeightValues.get(ruleWeightKey);
            List<RuleWeightVO.Award> awardList = new ArrayList<>();
            // 也可以修改为一次从数据库查询
            for (Integer awardId : awardIds) {
                StrategyAward strategyAwardReq = new StrategyAward();
                strategyAwardReq.setStrategyId(strategyId);
                strategyAwardReq.setAwardId(awardId);
                StrategyAward strategyAward = strategyAwardMapper.queryStrategyAward(strategyAwardReq);
                awardList.add(RuleWeightVO.Award.builder()
                        .awardId(strategyAward.getAwardId())
                        .awardTitle(strategyAward.getAwardTitle())
                        .build());
            }

            ruleWeightVOS.add(RuleWeightVO.builder()
                    .ruleValue(ruleValue)
                    .weight(Integer.valueOf(ruleWeightKey.split(Constants.COLON)[0]))
                    .awardIds(awardIds)
                    .awardList(awardList)
                    .build());
        }

        // 设置缓存 - 实际场景中，这类数据，可以在活动下架的时候统一清空缓存。
        redisService.setValue(cacheKey, ruleWeightVOS);

        return ruleWeightVOS;
    }


}
