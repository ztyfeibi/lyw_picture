package com.liyiwei.picturebase.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyiwei.picturebase.model.dto.space.spaceuser.SpaceUserAddRequest;
import com.liyiwei.picturebase.model.dto.space.spaceuser.SpaceUserQueryRequest;
import com.liyiwei.picturebase.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.vo.space.SpaceUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 16001
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-08-13 14:05:22
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     * @param addRequest
     * @return
     */
    Long addSpaceUser(SpaceUserAddRequest addRequest);

    /**
     * 校验空间用户信息
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);


    /**
     * 构建查询条件
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户视图对象
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间用户视图对象列表
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
