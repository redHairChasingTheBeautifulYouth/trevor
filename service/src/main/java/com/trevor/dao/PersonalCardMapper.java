package com.trevor.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 一句话描述该类作用:【】
 *
 * @author: trevor
 * @create: 2019-03-08 0:14
 **/
@Repository
public interface PersonalCardMapper {

    /**
     * 根据玩家查询玩家拥有的房卡数量
     * @param userId
     * @return
     */
    Integer findCardNumByUserId(@Param("userId") Long userId);

    /**
     * 根据玩家id更新房卡数量
     * @param userId
     * @param card
     */
    void updatePersonalCardNum(@Param("userId") Long userId ,@Param("card") Integer card);
}
