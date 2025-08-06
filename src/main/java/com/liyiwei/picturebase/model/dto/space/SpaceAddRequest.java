package com.liyiwei.picturebase.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;

}
