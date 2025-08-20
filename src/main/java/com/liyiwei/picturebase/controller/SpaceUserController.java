package com.liyiwei.picturebase.controller;

import cn.hutool.core.util.ObjUtil;
import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.DeleteRequest;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.dto.space.SpaceEditRequest;
import com.liyiwei.picturebase.model.dto.space.spaceuser.SpaceUserAddRequest;
import com.liyiwei.picturebase.model.dto.space.spaceuser.SpaceUserEditRequest;
import com.liyiwei.picturebase.model.dto.space.spaceuser.SpaceUserQueryRequest;
import com.liyiwei.picturebase.model.entity.SpaceUser;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.space.SpaceUserVO;
import com.liyiwei.picturebase.service.SpaceUserService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spaceUser")
@Slf4j
public class SpaceUserController {

    @Autowired
    private SpaceUserService spaceUserService;

    @Autowired
    private UserService userService;

    /**
     * 添加空间用户
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest addRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(addRequest);
        return ResultUtils.success(id);
    }

    /**
     * 删除空间用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        long id = deleteRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean res = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取某个空间的某个用户信息
     */
    @PostMapping("/get")
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = userQueryRequest.getSpaceId();
        Long userId = userQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(spaceUser);
    }

    /**
     * 获取空间用户列表
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest userQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(userQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑空间用户信息
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest editRequest, HttpServletRequest request) {
        if (editRequest == null || editRequest.getId() <= 0) throw new BusinessException(ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(editRequest, spaceUser);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        // 判断是否存在
        long id = editRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新数据库
        boolean res = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 查询加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
