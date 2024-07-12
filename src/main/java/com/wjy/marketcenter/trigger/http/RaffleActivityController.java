package com.wjy.marketcenter.trigger.http;

import com.alibaba.fastjson.JSON;
import com.wjy.marketcenter.api.IRaffleActivityService;
import com.wjy.marketcenter.api.dto.ActivityDrawRequestDTO;
import com.wjy.marketcenter.api.dto.ActivityDrawResponseDTO;
import com.wjy.marketcenter.api.dto.UserActivityAccountRequestDTO;
import com.wjy.marketcenter.api.dto.UserActivityAccountResponseDTO;
import com.wjy.marketcenter.common.Response;
import com.wjy.marketcenter.entity.RaffleAwardEntity;
import com.wjy.marketcenter.entity.RaffleFactorEntity;
import com.wjy.marketcenter.entity.activity.ActivityAccountEntity;
import com.wjy.marketcenter.entity.activity.UserRaffleOrderEntity;
import com.wjy.marketcenter.entity.award.UserAwardRecordEntity;
import com.wjy.marketcenter.entity.rebate.BehaviorEntity;
import com.wjy.marketcenter.entity.rebate.BehaviorRebateOrderEntity;
import com.wjy.marketcenter.enums.ResponseCode;
import com.wjy.marketcenter.exception.AppException;
import com.wjy.marketcenter.service.activity.IRaffleActivityAccountQuotaService;
import com.wjy.marketcenter.service.activity.IRaffleActivityPartakeService;
import com.wjy.marketcenter.service.activity.armory.IActivityArmory;
import com.wjy.marketcenter.service.award.IAwardService;
import com.wjy.marketcenter.service.rebate.IBehaviorRebateService;
import com.wjy.marketcenter.service.strategy.IRaffleStrategy;
import com.wjy.marketcenter.service.strategy.armory.IStrategyArmory;
import com.wjy.marketcenter.valobj.award.AwardStateVO;
import com.wjy.marketcenter.valobj.rebate.BehaviorTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 抽奖活动服务 注意；在不引用 application/case 层的时候，就需要让接口实现层来做领域的串联。一些较大规模的系统，需要加入 case 层。
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/raffle/activity/")
public class RaffleActivityController implements IRaffleActivityService {
    private final SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyyMMdd");
    @Resource
    private IRaffleActivityPartakeService raffleActivityPartakeService;
    @Resource
    private IRaffleStrategy raffleStrategy;
    @Resource
    private IAwardService awardService;
    @Resource
    private IActivityArmory activityArmory;
    @Resource
    private IStrategyArmory strategyArmory;

    @Resource
    private IBehaviorRebateService behaviorRebateService;

    @Resource
    private IRaffleActivityAccountQuotaService raffleActivityAccountQuotaService;



    /**
     * 活动装配 - 数据预热 | 把活动配置的对应的 sku 一起装配
     * 活动信息缓存：
     * 根据activityId查询活动sku信息，并缓存，key为sku，value为库存。
     * 根据suk信息中的activity_account_id查询活动参与次数增加规则【优先走缓存，未命中加载】。key为activityCountId，value为ActivityCountEntity
     * 根据activityId查询活动的信息。优先走缓存，未命中，写缓存。key为activityId，value为ActivityEntity。
     * 策略信息缓存：
     * 根据activityId查询出strategyId。然后根据策略id。 缓存奖品库存， key:strategyId_awardId,value:库存
     * 缓存该策略下抽奖时的生成随机数的最大值，key为策略id，value为最大值。
     * 缓存该策略下生成的随机数对应的奖品id，key为策略id，filed为生成的随机数，value为奖品id
     *
     * @param activityId 活动ID
     * @return 装配结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/armory">/api/v1/raffle/activity/armory</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     *
     * curl --request GET \
     *   --url 'http://localhost:8091/api/v1/raffle/activity/armory?activityId=100301'
     */
    @RequestMapping(value = "armory", method = RequestMethod.GET)
    @Override
    public Response<Boolean> armory(@RequestParam Long activityId) {
        try {
            log.info("活动装配，数据预热，开始 activityId:{}", activityId);
            // 1. 活动装配
            activityArmory.assembleActivitySkuByActivityId(activityId);
            // 2. 策略装配
            strategyArmory.assembleLotteryStrategyByActivityId(activityId);
            Response<Boolean> response = Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
            log.info("活动装配，数据预热，完成 activityId:{}", activityId);
            return response;
        } catch (Exception e) {
            log.error("活动装配，数据预热，失败 activityId:{}", activityId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 创建抽奖单
     *      * 1.根据userId、activityId查询用户的总、月、日抽奖次数，对总剩余抽奖次数校验
     *      * 2.根据userId、activityId、month查询用户的月抽奖次数
     *      *  2.1.如果有，对月抽奖次数校验
     *      *  2.2.如果没有，创建月抽奖次数对象
     *      * 3.根据userId、activityId、day查询用户的日抽奖次数
     *      *   3.1.如果有，对日抽奖次数校验
     *      *   3.2.如果没有，创建日抽奖次数对象
     *      * 4.由总、月、日抽奖次数、月抽奖次数、日抽奖次数、用户id、活动id构建创建抽奖单聚合对象
     *      * 5.根据userid查询活动信息，构建抽奖单对象（活动信息、用户id、订单号）
     *      * 6.抽奖单对象加入聚合对象中
     *      * 7.保存聚合对象
     *      *  7.1. 根据userId、activityId、总剩余抽奖次数大于0更新raffle_activity_account表，将总剩余抽奖次数都减一。
     *      *  7.2. 更新或新建月次数记录
     *      *   7.3. 存在月次数记录。
     *      *       7.3.1. 根据userId、activityId、month、month_count_surplus>0更新raffle_activity_account_month表，将表中的month_count_surplus减一
     *      *       7.3.2  根据根据userId、activityId、month_count_surplus>0更新raffle_activity_account表，将表中的month_count_surplus减一
     *      *   7.4. 不存在月次数记录。
     *      *       7.4.1. 新建月次数记录，月总次数为总次数表中的月总次数，月剩余次为总次数表中的月剩余次数减一。
     *      *       7.4.2. 根据userId、activityId更新raffle_activity_account表，将月剩余次数更新为之前查出来的月剩余次数减一。
     *      *  8. 更新或新建日次数记录（同月次数处理逻辑）
     *      *  9. 新建抽奖单记录
     * 执行抽奖，前置工作流
     *      * 1. 根据strategyId拿到责任链
     *      *  1.1 根据strategyId查strategy表，获得该策略配置的规则
     *      *  1.2 根据配置的规则，装配责任链返回
     *      * 2. 执行责任链
     *      *  2.1 黑名单处理器
     *      *      2.1.1. 根据策略id、规则名称为黑名单查询strategy_rule表，获得具体规则配置
     *      *      2.1.2. 解析配置，判断用户是否在黑名单中，如果在则返回兜底奖励，不在放行
     *      *  2.2 积分处理器
     *      *      2.2.1. 根据策略id、规则名称为黑名单查询strategy_rule表，获得具体规则配置
     *      *      2.2.2. 解析配置，如果用户的积分大于等于某个积分值，执行积分值内规定的奖品抽奖，否则放行
     *      *  2.3 默认处理器
     *      *      2.3.1. 执行默认抽奖
     * 执行抽奖，后置工作流
     *      * 1.根据策略id、奖品id，strategy_award表中查询后置规则树id，如果规则树id不存在，返回奖品
     *      * 2. 查询规则树信息
     *      *  2.1 根据treeId查rule_tree表，得到得到规则树的详细信息
     *      *  2.2 根据treeId查询rule_tree_node表，得到规则树下的所有结点
     *      *  2.3 根据treeId查rule_tree_node_line表，得到规则树中的所有边
     *      * 3. 组装树对象
     *      *  3.1. 把所有边组装到边map中，key为结点的起点的名字，value为起点相同的边的list
     *      *  3.2. 把所有点组装到点map中，key为结点的名字，value为结点对象。结点对象中由上一步边map中的list
     *      *  3.3. 组装树对象，树对象中，有根节点对象，和上一步的点map
     *      * 4. 创建规则树引擎，并执行
     *      *  4.1. 次数锁处理器：
     *            4.1.1. 查询日抽奖次数表，得到抽奖次数
     *            4.1.2. 判断用户的日抽奖次数，是否达到规定
     *      *  4.2 库存处理器：
     *      *      4.2.1. redissonClient.getAtomicLong(key).decrementAndGet();进行库存扣减，扣减完大于等于0，成功，否则失败
     *      *      4.2.2. 扣减成功对扣减成功后的库存加锁，兜底，防止超卖。
     *      *      4.2.3. 扣减库存成功，将库存扣减对象加入队列中（redis）。
     *      *      4.2.4. 定时任务根据活动id、策略id更新strategy_award表，award_count_surplus-1
     *      *  4.3 兜底奖励处理器：返回兜底奖励
     * 中奖结果落库
     *      * 1. 构建中奖事件消息对象
     *      * 2. 构建中奖任务对象
     *      * 3. 构建聚合对象（用户中奖对象，中奖任务对象），落库
     *      *   3.1. user_award_record表中新增记录
     *      *   3.2. task表中新增记录，状态为created
     *      *   3.3.  更新抽奖单状态为used
     *      *   3.4. 发送中奖MQ
     *      *   3.5. 更新task记录，状态变更为completed
     *      *   3.6. 如果发送MQ失败，更新task记录，状态变更为fail
     *      *   3.7. 定时任务，扫描task表，重新执行3，4步操作
     * @param request 请求对象
     * @return 抽奖结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/draw">/api/v1/raffle/activity/draw</a>
     * 入参：{"activityId":100001,"userId":"xiaofuge"}
     *
     * curl --request POST \
     *   --url http://localhost:8091/api/v1/raffle/activity/draw \
     *   --header 'content-type: application/json' \
     *   --data '{
     *     "userId":"xiaofuge",
     *     "activityId": 100301
     * }'
     */
    @RequestMapping(value = "draw", method = RequestMethod.POST)
    @Override
    public Response<ActivityDrawResponseDTO> draw(@RequestBody ActivityDrawRequestDTO request) {
        try {
            log.info("活动抽奖 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
            // 1. 参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            // 2. 参与活动 - 创建参与记录订单
            UserRaffleOrderEntity orderEntity = raffleActivityPartakeService.createOrder(request.getUserId(), request.getActivityId());
            log.info("活动抽奖，创建订单 userId:{} activityId:{} orderId:{}", request.getUserId(), request.getActivityId(), orderEntity.getOrderId());
            // 3. 抽奖策略 - 执行抽奖
            RaffleAwardEntity raffleAwardEntity = raffleStrategy.performRaffle(RaffleFactorEntity.builder()
                    .userId(orderEntity.getUserId())
                    .strategyId(orderEntity.getStrategyId())
                    .endDateTime(orderEntity.getEndDateTime())
                    .build());
            // 4. 存放结果 - 写入中奖记录
            UserAwardRecordEntity userAwardRecord = UserAwardRecordEntity.builder()
                    .userId(orderEntity.getUserId())
                    .activityId(orderEntity.getActivityId())
                    .strategyId(orderEntity.getStrategyId())
                    .orderId(orderEntity.getOrderId())
                    .awardId(raffleAwardEntity.getAwardId())
                    .awardTitle(raffleAwardEntity.getAwardTitle())
                    .awardTime(new Date())
                    .awardState(AwardStateVO.create)
                    .awardConfig(raffleAwardEntity.getAwardConfig())
                    .build();
            awardService.saveUserAwardRecord(userAwardRecord);
            // 5. 返回结果
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(ActivityDrawResponseDTO.builder()
                            .awardId(raffleAwardEntity.getAwardId())
                            .awardTitle(raffleAwardEntity.getAwardTitle())
                            .awardIndex(raffleAwardEntity.getSort())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("活动抽奖失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<ActivityDrawResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 日历签到返利接口
     *
     * @param userId 用户ID
     * @return 签到返利结果
     * <p>
     * 接口：<a href="http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate">/api/v1/raffle/activity/calendar_sign_rebate</a>
     * 入参：xiaofuge
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value = "calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> calendarSignRebate(@RequestParam String userId) {
        try {
            log.info("日历签到返利开始 userId:{}", userId);
            BehaviorEntity behaviorEntity = new BehaviorEntity();
            behaviorEntity.setUserId(userId);
            behaviorEntity.setBehaviorTypeVO(BehaviorTypeVO.SIGN);
            behaviorEntity.setOutBusinessNo(dateFormatDay.format(new Date()));
            List<String> orderIds = behaviorRebateService.createOrder(behaviorEntity);
            log.info("日历签到返利完成 userId:{} orderIds: {}", userId, JSON.toJSONString(orderIds));
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.error("日历签到返利异常 userId:{} ", userId, e);
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("日历签到返利失败 userId:{}", userId);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }


    /**
     * 根据userid、outBusinessNo查询user_behavior_rebate_order表，得到用户的返利记录
     * 如果有返利记录，说明已经做了返利
     * <p>
     * curl -X POST http://localhost:8091/api/v1/raffle/activity/is_calendar_sign_rebate -d "userId=xiaofuge" -H "Content-Type: application/x-www-form-urlencoded"
     */
    @RequestMapping(value = "is_calendar_sign_rebate", method = RequestMethod.POST)
    @Override
    public Response<Boolean> isCalendarSignRebate(String userId) {
        try {
            log.info("查询用户是否完成日历签到返利开始 userId:{}", userId);
            String outBusinessNo = dateFormatDay.format(new Date());
            List<BehaviorRebateOrderEntity> behaviorRebateOrderEntities = behaviorRebateService.queryOrderByOutBusinessNo(userId, outBusinessNo);
            log.info("查询用户是否完成日历签到返利完成 userId:{} orders.size:{}", userId, behaviorRebateOrderEntities.size());
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(!behaviorRebateOrderEntities.isEmpty()) // 只要不为空，则表示已经做了签到
                    .build();
        } catch (Exception e) {
            log.error("查询用户是否完成日历签到返利失败 userId:{}", userId, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }

    /**
     * 查询账户额度
     *1.查询用户抽奖次数
     * 1.1 根据用户id、活动id查询raffle_activity_account表，得到用户在该活动的抽奖次数，不存再记录，创建兜底对象（次数都是0）。
     * 1.2 根据userId、activityId、month查raffle_activity_account_month表，获得用户在该活动上某月的抽奖次数
     * 1.3 根据用户id、活动id、day查询raffle_activity_account_day表，得到用户的日次数
     *
     *
     *
     * <p>
     * curl --request POST \
     * --url http://localhost:8091/api/v1/raffle/activity/query_user_activity_account \
     * --header 'content-type: application/json' \
     * --data '{
     * "userId":"xiaofuge",
     * "activityId": 100301
     * }'
     */
    @RequestMapping(value = "query_user_activity_account", method = RequestMethod.POST)
    @Override
    public Response<UserActivityAccountResponseDTO> queryUserActivityAccount(UserActivityAccountRequestDTO request) {
        try {
            log.info("查询用户活动账户开始 userId:{} activityId:{}", request.getUserId(), request.getActivityId());
            // 1. 参数校验
            if (StringUtils.isBlank(request.getUserId()) || null == request.getActivityId()) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
            }
            ActivityAccountEntity activityAccountEntity = raffleActivityAccountQuotaService.queryActivityAccountEntity(request.getActivityId(), request.getUserId());
            UserActivityAccountResponseDTO userActivityAccountResponseDTO = UserActivityAccountResponseDTO.builder()
                    .totalCount(activityAccountEntity.getTotalCount())
                    .totalCountSurplus(activityAccountEntity.getTotalCountSurplus())
                    .dayCount(activityAccountEntity.getDayCount())
                    .dayCountSurplus(activityAccountEntity.getDayCountSurplus())
                    .monthCount(activityAccountEntity.getMonthCount())
                    .monthCountSurplus(activityAccountEntity.getMonthCountSurplus())
                    .build();
            log.info("查询用户活动账户完成 userId:{} activityId:{} dto:{}", request.getUserId(), request.getActivityId(), JSON.toJSONString(userActivityAccountResponseDTO));
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(userActivityAccountResponseDTO)
                    .build();
        } catch (Exception e) {
            log.error("查询用户活动账户失败 userId:{} activityId:{}", request.getUserId(), request.getActivityId(), e);
            return Response.<UserActivityAccountResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


}
