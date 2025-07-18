package com.liyiwei.picturebase.controller;

import com.liyiwei.picturebase.annotation.AuthCheck;
import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.manager.CosManager;
import com.liyiwei.picturebase.model.constant.UserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private CosManager cosManager;

//    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file")MultipartFile multipartFile) {
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s",fileName);

        File file = null;
        try {
            file = File.createTempFile(filePath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath,file);
            return ResultUtils.success(filePath);
        }catch (Exception e){
            log.error("file upload error, filepath= "+filePath,e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            if (file!=null){
                boolean delete = file.delete();
                if (!delete){
                    log.error("file delete error,filepath={}",filePath);
                }
            }
        }
    }
}
