package com.liyiwei.picturebase.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureSearchByPRequest implements Serializable {

    /**
     * 图片ID
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
