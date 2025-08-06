package com.liyiwei.picturebase.service;

import com.liyiwei.picturebase.model.dto.space.SpaceAddRequest;
import com.liyiwei.picturebase.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.entity.User;

/**
* @author 16001
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-08-03 21:39:49
*/
public interface SpaceService extends IService<Space> {

    /**
     * 校验空间数据，add区别是创建时校验还是编辑时校验
     * @param space
     * @param add
     */
    void validSpace(Space space,boolean add);

    /**
     * 在创建、更新空间时，根据空间级别自动填充数据
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 检查空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
