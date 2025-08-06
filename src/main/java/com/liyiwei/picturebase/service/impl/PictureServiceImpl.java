package com.liyiwei.picturebase.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.manager.CosManager;
import com.liyiwei.picturebase.manager.FileManager;
import com.liyiwei.picturebase.model.dto.file.UploadPictureResult;
import com.liyiwei.picturebase.model.dto.picture.*;
import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.enums.PictureReviewStatusEnum;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import com.liyiwei.picturebase.model.vo.UserVO;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.mapper.PictureMapper;
import com.liyiwei.picturebase.service.SpaceService;
import com.liyiwei.picturebase.service.UserService;
import com.liyiwei.picturebase.utils.ColorsSimilarityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
* @author 16001
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-07-17 21:24:54
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;
    @Autowired
    private UserService userService;
    @Autowired
    private CosManager cosManager;
    @Autowired
    private SpaceService spaceService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Resource
    private ThreadPoolExecutor customExecutor;

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) return queryWrapper;

        Long id = pictureQueryRequest.getId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Boolean nullSpaceId = pictureQueryRequest.getNullSpaceId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            //需要拼接查询条件
            queryWrapper.and(qw->qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId),"spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId,"spaceId");
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        queryWrapper.eq(ObjUtil.isNotEmpty(id),"id",id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId),"userId",userId);
        queryWrapper.like(StrUtil.isNotBlank(name),"name",name);
        queryWrapper.like(StrUtil.isNotBlank(introduction),"introduction",introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat),"picFormat",picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category),"category",category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize),"picSize",picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth),"picWidth",picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight),"picHeight",picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale),"picScale",picScale);
        // json数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags","\""+tag+"\"");//精确匹配
            }
        }

        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    //todo
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            // 必须空间创始人、管理员才能上传
            if (!loginUser.getId().equals(space.getUserId())) throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间权限");
            // 额度校验
            if (space.getTotalCount() >= space.getMaxCount()) throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间条数不足");
            if (space.getTotalSize() >= space.getMaxSize()) throw new BusinessException(ErrorCode.OPERATION_ERROR,"空间大小不足");
        }

        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) pictureId = pictureUploadRequest.getId();

        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            // 仅本人、管理员可以更新图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            if (spaceId == null){
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            }else{
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间不一致，不能更新图片");
                }
            }
        }

        // 上传图片，得到信息
        // 按照用户 id 划分目录
        String uploadPathPrefix;
        if (spaceId == null){
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }else{
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setSpaceId(spaceId);
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        // 补充审核参数
        fillReviewParams(picture, loginUser);

        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if (finalSpaceId != null) {
                // 更新空间使用量
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize+"+picture.getPicSize())
                        .setSql("totalCount = totalCount+1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "空间使用量更新失败");
            }
            return picture;
        });

        return PictureVO.obj2Vo(picture);
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.obj2Vo(picture);
        Long userid = picture.getUserId();
        if (userid != null && userid > 0) {
            User user = userService.getById(userid);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();

        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) return pictureVOPage;

        //TODO toList而不是原来的Collectors
        // 对象列表 -》 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::obj2Vo)
                .toList();
        // 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId)
                .collect(Collectors.toSet());
        Map<Long,List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR,"id cannot null");
        if (StrUtil.isNotBlank(url)) ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片链接过长");
        if (StrUtil.isNotBlank(introduction)) ThrowUtils.throwIf(introduction.length() > 1000, ErrorCode.PARAMS_ERROR, "图片介绍过长");
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture==null, ErrorCode.NOT_FOUND_ERROR);
        // 重复
        if (oldPicture.getReviewStatus().equals(reviewStatus))
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片已审核过");

        // 更新状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean res = this.updateById(updatePicture);
        ThrowUtils.throwIf(res, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            // 普通用户提交审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public void deletePicture(Long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0,ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        checkPictureAuth(oldPicture, loginUser);

        checkPictureAuth(oldPicture, loginUser);
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean res = this.removeById(pictureId);
            ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize-"+oldPicture.getPicSize())
                        .setSql("totalCount = totalCount-1")
                        .update();
                ThrowUtils.throwIf(update, ErrorCode.OPERATION_ERROR,"额度更新失败");
            }
            return true;
        });

        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();

        if (count > 1) return;

        // 取key
        cosManager.deleteObject(oldPicture.getUrl());
        // 清理缩略图
        // ...
    }

    @Override
    public void checkPictureAuth(Picture picture, User loginUser) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null){
            // 公共图库，仅本人或管理员
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }else{
            // 私有空间
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void editPicture(PictureEditRequest editRequest, User loginUser) {
        // 类转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(editRequest, picture);
        // list 2 string
        picture.setTags(JSONUtil.toJsonStr(editRequest.getTags()));
        // 编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 是否存在
        long id = editRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 权限
        checkPictureAuth(oldPicture, loginUser);
        // 审核参数
        //todo 和别的填充一起合并？
        this.fillReviewParams(picture, loginUser);
        // 数据库
        boolean res = this.updateById(picture);
        ThrowUtils.throwIf(res, ErrorCode.OPERATION_ERROR);
    }

    //todo
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor),ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NO_AUTH_ERROR);
        // 空间校验
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间访问权限");
        // 查询空间下所以图片（必须有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId,space)
                .isNotNull(Picture::getPicColor)
                .list();
        if (CollUtil.isEmpty(pictureList)) return Collections.emptyList();

        // 目标颜色转换为Color对象
        Color targetColor = Color.decode(picColor);
        // 计算相似度并排序
        List<Picture> sortedPics = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture->{
                    String hexColor = picture.getPicColor();
                    if (StrUtil.isBlank(hexColor)) return Double.MAX_VALUE;
                    Color pictureColor = Color.decode(hexColor);
                    return  -ColorsSimilarityUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                .limit(12)
                .collect(Collectors.toList());

        return sortedPics.stream()
                .map(PictureVO::obj2Vo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest editByBatchRequest, User loginUser) {
        List<Long> pictureIdList = editByBatchRequest.getPictureIdList();
        Long spaceId = editByBatchRequest.getSpaceId();
        String category = editByBatchRequest.getCategory();
        List<String> tags = editByBatchRequest.getTags();

        // 校验
        ThrowUtils.throwIf(spaceId==null || CollUtil.isEmpty(pictureIdList),ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null,ErrorCode.NO_AUTH_ERROR);
        // 空间
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        if (!loginUser.getId().equals(space.getUserId()))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间访问权限");

        //todo 看不懂
        // 查询指定图片，只要特定的字段
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId,Picture::getSpaceId)
                .eq(Picture::getSpaceId,space)
                .in(Picture::getId,pictureIdList)
                .list();

        if (pictureList.isEmpty()) return;

        // 更新
        pictureList.forEach(picture->{
            if (StrUtil.isNotBlank(category))
                picture.setCategory(category);
            if (CollUtil.isNotEmpty(tags))
                picture.setTags(JSONUtil.toJsonStr(tags));
        });

        // 批量更新
        boolean res = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(res, ErrorCode.OPERATION_ERROR);
    }

    /** todo 异步分批
     * 批量编辑图片分类和标签
     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void batchEditPictureMetadata(PictureBatchEditRequest request, Long spaceId, Long loginUserId) {
//        // 参数校验
//        validateBatchEditRequest(request, spaceId, loginUserId);
//
//        // 查询空间下的图片
//        List<Picture> pictureList = this.lambdaQuery()
//                .eq(Picture::getSpaceId, spaceId)
//                .in(Picture::getId, request.getPictureIds())
//                .list();
//
//        if (pictureList.isEmpty()) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "指定的图片不存在或不属于该空间");
//        }
//
//        // 分批处理避免长事务
//        int batchSize = 100;
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        for (int i = 0; i < pictureList.size(); i += batchSize) {
//            List<Picture> batch = pictureList.subList(i, Math.min(i + batchSize, pictureList.size()));
//
//            // 异步处理每批数据
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                batch.forEach(picture -> {
//                    // 编辑分类和标签
//                    if (request.getCategory() != null) {
//                        picture.setCategory(request.getCategory());
//                    }
//                    if (request.getTags() != null) {
//                        picture.setTags(String.join(",", request.getTags()));
//                    }
//                });
//                boolean result = this.updateBatchById(batch);
//                if (!result) {
//                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量更新图片失败");
//                }
//            }, customExecutor);
//
//            futures.add(future);
//        }
//
//        // 等待所有任务完成
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//    }

    @Override
    public void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule) ) return;

        long count = 1;
        try{
            for (Picture picture : pictureList) {
                String picName = nameRule.replaceAll("\\{序号}",String.valueOf(count++));
                picture.setName(picName);
            }
        }catch (Exception e){
            log.error("名称解析错误",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"名称解析错误");
        }
    }
}




