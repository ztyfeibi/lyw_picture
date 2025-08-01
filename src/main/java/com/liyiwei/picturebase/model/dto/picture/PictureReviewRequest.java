package com.liyiwei.picturebase.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReviewRequest implements Serializable {

    /**
     * Picture ID
     */
    private Long id;

    /**
     * Review status: 0 for pending, 1 for approved, 2 for rejected
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    private static final long serialVersionUID = 1L;
}
