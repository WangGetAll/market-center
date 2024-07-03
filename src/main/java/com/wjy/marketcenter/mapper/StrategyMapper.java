package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.entity.Strategy;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StrategyMapper {
    List<Strategy> queryStrategyList();
}
