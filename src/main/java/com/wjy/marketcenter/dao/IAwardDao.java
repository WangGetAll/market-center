package com.wjy.marketcenter.dao;

import com.wjy.marketcenter.po.Award;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface IAwardDao {
    List<Award> queryAwardList();

    /**
     * 根据awardId查询award表，得到awardConfig
     * @param awardId
     * @return
     */
    String queryAwardConfigByAwardId(Integer awardId);

    /**
     * 根据awardId查询award表，得到awardKey
     * @param awardId
     * @return
     */
    String queryAwardKeyByAwardId(Integer awardId);

}
