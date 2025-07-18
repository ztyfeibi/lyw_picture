package com.liyiwei.picturebase.service;

import com.liyiwei.picturebase.model.dto.picture.PictureUploadRequest;
import com.liyiwei.picturebase.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
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
     * 上传图片
     *
     * @param multipartFile
     * @param request
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User loginUser);
}
