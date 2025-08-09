package com.liyiwei.picturebase.model.vo.space;

import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.vo.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceVo implements Serializable {

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

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     * @param spaceVo
     * @return
     */
    public static Space vo2Obj(SpaceVo spaceVo){
        if (spaceVo == null) return null;

        Space space = new Space();
        BeanUtils.copyProperties(spaceVo, space);
        return space;
    }

    /**
     * 对象转封装类
     * @param space
     * @return
     */
    public static SpaceVo obj2Vo(Space space){
        if (space == null) return null;

        SpaceVo spaceVo = new SpaceVo();
        BeanUtils.copyProperties(space, spaceVo);
        return spaceVo;
    }
}