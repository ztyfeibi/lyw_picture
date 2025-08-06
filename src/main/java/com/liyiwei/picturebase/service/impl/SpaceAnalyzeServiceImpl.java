package com.liyiwei.picturebase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.analyze.SpaceAnalyzeRequest;
import com.liyiwei.picturebase.service.SpaceAnalyzeService;
import com.liyiwei.picturebase.service.SpaceService;
import com.liyiwei.picturebase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Autowired
    private UserService userService;
    @Autowired
    private SpaceService spaceService;

    @Override
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // check auth
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()){
            // 全空间分析或者公共图库权限校验: 仅管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,"无权访问");
        }else{
            // 私有
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId==null || spaceId <= 0,ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            spaceService.checkSpaceAuth(loginUser,space);
        }
    }

    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {}
}
