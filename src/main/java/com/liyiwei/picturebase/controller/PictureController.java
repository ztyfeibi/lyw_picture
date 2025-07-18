package com.liyiwei.picturebase.controller;

import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.model.dto.picture.PictureUploadRequest;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/picture")
public class PictureController {

    @Autowired
    PictureService pictureService;

    @Autowired
    private UserService userService;

    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest servletRequest){
        User loginUser = userService.getLoginUser(servletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);
    }
}
