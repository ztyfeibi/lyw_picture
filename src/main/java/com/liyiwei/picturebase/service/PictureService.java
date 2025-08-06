package com.liyiwei.picturebase.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liyiwei.picturebase.model.dto.picture.*;
import com.liyiwei.picturebase.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 设置图片审核状态，管理员自动过审。。。
     * @param picture
     * @return
     */
    void fillReviewParams(Picture picture,User loginUser);

    /**
     * 删除图片
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(Long pictureId, User loginUser);

    /**
     * 清除图片文件
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 校验用户的图片权限
     * @param picture
     * @param loginUser
     */
    void checkPictureAuth(Picture picture, User loginUser);

    /**
     * 编辑图片
     * @param editRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest editRequest,User loginUser);

    /**
     * 根据颜色查询图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId,String picColor,User loginUser);

    /**
     * 批量操作
     * @param editByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest editByBatchRequest,User loginUser);

//    /**
//     * 多线程批量操作
//     * @param request
//     * @param spaceId
//     * @param loginUserId
//     */
//    void batchEditPictureMetadata(PictureBatchEditRequest request, Long spaceId, Long loginUserId)

    /**
     * nameRule格式： 图片(序号)
     * @param pictureList
     * @param nameRule
     */
    void fillPictureWithNameRule(List<Picture> pictureList,String nameRule);
}
