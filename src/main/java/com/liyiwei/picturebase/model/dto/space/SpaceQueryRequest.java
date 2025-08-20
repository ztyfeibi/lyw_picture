package com.liyiwei.picturebase.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceQueryRequest implements Serializable {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;


    private static final long serialVersionUID = 1L;
}
