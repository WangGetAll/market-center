package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.entity.StrategyAward;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyAwardMapper {
    List<StrategyAward> queryStrategyAwardList();
}
