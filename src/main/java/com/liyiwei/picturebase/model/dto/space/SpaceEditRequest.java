package com.liyiwei.picturebase.model.dto.space;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;

}
