package com.trevor.domain;

import lombok.Data;

import java.util.List;

/**
 * 一句话描述该类作用:【用户提议，异常举报】
 *
 * @author: trevor
 * @create: 2019-03-05 0:16
 **/
@Data
public class UserProposals {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 提议或异常信息
     */
    private String message;

    /**
     * 照片的url,json字符串
     */
    private String fileUrls;

}
