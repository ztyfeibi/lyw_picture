package com.liyiwei.picturebase.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUpdateRequest implements Serializable {

    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
