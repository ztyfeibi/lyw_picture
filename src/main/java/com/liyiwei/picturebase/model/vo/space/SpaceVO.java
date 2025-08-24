package com.liyiwei.picturebase.model.vo.space;

import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.vo.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class SpaceVO implements Serializable {

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

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;

    /**
     * 空间权限列表
     */
    private List<String> permissionList = new ArrayList<>();


    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     * @param spaceVo
     * @return
     */
    public static Space vo2Obj(SpaceVO spaceVo){
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
    public static SpaceVO obj2Vo(Space space){
        if (space == null) return null;

        SpaceVO spaceVo = new SpaceVO();
        BeanUtils.copyProperties(space, spaceVo);
        return spaceVo;
    }
}