package com.liyiwei.picturebase.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liyiwei.picturebase.model.dto.picture.PictureQueryRequest;
import com.liyiwei.picturebase.model.dto.picture.PictureUploadRequest;
import com.liyiwei.picturebase.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
* @author 16001
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-17 21:24:54
*/

@Service
public interface PictureService extends IService<Picture> {

    /**
     * 将查询请求转换为QueryWrapper
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param request
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User loginUser);

    /**
     * 获取单个图片的VO对象
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片分页VO对象
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片数据
     * @param picture
     */
    void validPicture(Picture picture);
}
