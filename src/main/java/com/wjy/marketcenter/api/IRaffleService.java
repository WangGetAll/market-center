package com.wjy.marketcenter.api;

import com.wjy.marketcenter.api.dto.RaffleAwardListRequestDTO;
import com.wjy.marketcenter.api.dto.RaffleAwardListResponseDTO;
import com.wjy.marketcenter.api.dto.RaffleRequestDTO;
import com.wjy.marketcenter.api.dto.RaffleResponseDTO;
import com.wjy.marketcenter.common.Response;

import java.util.List;

/**
 *  抽奖服务接口
 */
public interface IRaffleService {

    /**
     * 策略装配接口
     *
     * @param strategyId 策略ID
     * @return 装配结果
     */
    Response<Boolean> strategyArmory(Long strategyId);

    /**
     * 查询抽奖奖品列表配置
     *
     * @param requestDTO 抽奖奖品列表查询请求参数
     * @return 奖品列表数据
     */
    Response<List<RaffleAwardListResponseDTO>> queryRaffleAwardList(RaffleAwardListRequestDTO requestDTO);

    /**
     * 随机抽奖接口
     *
     * @param requestDTO 请求参数
     * @return 抽奖结果
     */
    Response<RaffleResponseDTO> randomRaffle(RaffleRequestDTO requestDTO);

}