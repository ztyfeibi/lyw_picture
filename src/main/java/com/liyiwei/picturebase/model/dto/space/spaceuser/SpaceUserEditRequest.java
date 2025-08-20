package com.liyiwei.picturebase.model.dto.space.spaceuser;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
