package com.liyiwei.picturebase.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.liyiwei.picturebase.config.CosClientConfig;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param file
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile file,String uploadPathPrefix) {
        //校验图片
        validPicture(file);

        String uuid = RandomUtil.randomString(16);
        String originFileName = file.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()),uuid, FileUtil.getSuffix(originFileName));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix,uploadFileName);
        File resFile = null;

        try{
            //创建临时文件
            resFile = File.createTempFile(uploadPath,null);
            file.transferTo(resFile);
            //上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, resFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth*1.0 / picHeight,2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originFileName));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(resFile));
            uploadPictureResult.setUrl(cosClientConfig.getHost());

            return uploadPictureResult;
        }catch (Exception e){
            log.error("图片上传到cos失败",e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败");
        }finally {
            this.deletTempleFile(resFile);
        }
    }


    /**
     * 校验文件
     *
     * @param file
     */
    private void validPicture(MultipartFile file) {
        ThrowUtils.throwIf(file == null,ErrorCode.PARAMS_ERROR);
        //1. 校验文件大小
        long fileSize = file.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2*ONE_M,ErrorCode.PARAMS_ERROR);
        //2. 校验文件后最
        String fileSuffix = FileUtil.getSuffix(file.getOriginalFilename());
        final List<String> ALLOW_FORMAT = Arrays.asList("jpeg","jpg","png","wwbp","bmp");
        ThrowUtils.throwIf(ALLOW_FORMAT.contains(fileSuffix),ErrorCode.PARAMS_ERROR);
    }


    /**
     * 删除临时文件
     *
     * @param resFile
     */
    private void deletTempleFile(File resFile) {
        if (resFile == null) return;

        boolean deleteRes = resFile.delete();
        if (!deleteRes)
            log.error("file delete failed, file path: {}", resFile.getAbsolutePath());
    }



}
