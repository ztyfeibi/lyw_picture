package com.liyiwei.picturebase.model.vo;

import cn.hutool.json.JSONUtil;
import com.liyiwei.picturebase.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 部分属性用Integer、Long声明，因为这样就允许为空/null
 */

@Data
public class PictureVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片url
     */
    private String url;

    /**
     * 图片名字
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 图片标签
     */
    private List<String> tags;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片尺寸
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 用户id
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
     * 创建用户的信息
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     * @param pictureVO
     * @return
     */
    public static Picture vo2Obj(PictureVO pictureVO) {
        if (pictureVO == null) return null;
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(picture.getTags()));
        return picture;
    }

    /**
     * 对象封装类
     * @param picture
     * @return
     */
    public static PictureVO obj2Vo(Picture picture) {
        if (picture == null) return null;
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
