package com.wjy.marketcenter.mapper;

import com.wjy.marketcenter.po.Award;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AwardMapper {
    List<Award> queryAwardList();
}
