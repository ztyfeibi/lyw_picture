package com.liyiwei.picturebase.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已经有了User类，为什么还需要VO？
 * 1、脱敏，有些信息不需要放回给前端
 * 2、方便组合信息，扩展性强。
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
