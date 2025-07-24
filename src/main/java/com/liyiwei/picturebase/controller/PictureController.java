package com.liyiwei.picturebase.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liyiwei.picturebase.annotation.AuthCheck;
import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.DeleteRequest;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.constant.UserConstant;
import com.liyiwei.picturebase.model.dto.picture.PictureEditRequest;
import com.liyiwei.picturebase.model.dto.picture.PictureQueryRequest;
import com.liyiwei.picturebase.model.dto.picture.PictureUpdateRequest;
import com.liyiwei.picturebase.model.dto.picture.PictureUploadRequest;
import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.PictureTagCategory;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Autowired
    PictureService pictureService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest servletRequest){
        User loginUser = userService.getLoginUser(servletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        boolean res = pictureService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除图片失败");

        return ResultUtils.success(res);
    }

    /**
     * 更新图片 仅管理员
     * @param updateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest updateRequest){
        if (updateRequest == null || updateRequest.getId() <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Picture p = new Picture();
        BeanUtils.copyProperties(updateRequest, p);
        // 传入的是list，变成json字符串以后，传入实体


        pictureService.validPicture(p);
        long id = updateRequest.getId();
        Picture oldP = pictureService.getById(id);
        ThrowUtils.throwIf(oldP == null, ErrorCode.NOT_FOUND_ERROR);

        boolean res = pictureService.updateById(p);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新图片失败");
        return ResultUtils.success(res);
    }

    /**
     * 根据id获取图片 管理员
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id){
        ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
        Picture p = pictureService.getById(id);
        ThrowUtils.throwIf(p == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(p);
    }


    /**
     * 根据id获取图片 VO
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id,HttpServletRequest request){
        ThrowUtils.throwIf(id <= 0,ErrorCode.PARAMS_ERROR);
        Picture p = pictureService.getById(id);
        ThrowUtils.throwIf(p == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(pictureService.getPictureVO(p,request));
    }

    /**
     * 分页获取图片列表 管理员
     * @param queryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest queryRequest){
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(queryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表 VO
     * @param queryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest queryRequest,
                                                             HttpServletRequest request){
        long current = queryRequest.getCurrent();
        long size = queryRequest.getPageSize();

        ThrowUtils.throwIf(size > 20,ErrorCode.PARAMS_ERROR);

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(queryRequest));

        return ResultUtils.success(pictureService.getPictureVOPage(picturePage,request));
    }

    /**
     * 编辑图片
     * @param editRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest editRequest,HttpServletRequest request){
        if (editRequest == null || editRequest.getId() <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);

        Picture picture = new Picture();
        BeanUtils.copyProperties(editRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(editRequest.getTags()));
        picture.setEditTime(new Date());

        pictureService.validPicture(picture);

        long id = editRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        User loginUser = userService.getLoginUser(request);
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);

        boolean res = pictureService.updateById(picture);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(res);
    }

    /**
     * return 默认图片标签
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


}
