package com.liyiwei.picturebase.auth;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.liyiwei.picturebase.auth.stpUtil.StpKit;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.model.constant.UserConstant;
import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.entity.SpaceUser;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.enums.SpaceRoleEnum;
import com.liyiwei.picturebase.model.enums.SpaceTypeEnum;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.service.SpaceService;
import com.liyiwei.picturebase.service.SpaceUserService;
import com.liyiwei.picturebase.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SpaceUserAuthManager spaceUserAuthManager;
    private final SpaceUserService spaceUserService;
    private final PictureService pictureService;
    private final UserService userService;
    private final SpaceService spaceService;

    public List<String> getPermissionList(Object loginId, String loginType){
        // 仅对类型为 space 进行校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) return new ArrayList<>();
        // 管理员权限，表示校验通过
        List<String> ADMIN_PERMISSION_LIST = spaceUserAuthManager.getPermissionByRole(SpaceRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        SpaceUserAuthContext spaceUserAuthContext = new SpaceUserAuthContext();
        SpaceUserAuthContext authContext = spaceUserAuthContext.getAuthContextByRequest();
        // 如果所有字段都为空，表示查询公共图库 通过
        if (isAllFieldsNull(authContext)) return ADMIN_PERMISSION_LIST;
        // 获取userid
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"用户未登录");

        Long userId = loginUser.getId();
        // 优先从上下文中获取 SpaceUser
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) return spaceUserAuthManager.getPermissionByRole(spaceUser.getSpaceRole());
        // 如果有spaceUserId，必然是团队空间,通过数据库查询spaceUser对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null){
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUserId == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"空间用户不存在");
            // 取出当前登录用户对应的spaceUser;
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId,spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId,userId)
                    .one();
            if (loginSpaceUser == null) return new ArrayList<>();
            // 这里会导致管理员在私有空间没有权限，可以再查一次库
            return spaceUserAuthManager.getPermissionByRole(loginSpaceUser.getSpaceRole());
        }
        // 如果没有 spaceUserId，尝试通过spaceid 或 pictureId 获取 Space 对象
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            Long pictureId = authContext.getPictureId();
            // 图片id也米有，默认通过
            if (pictureId == null) return ADMIN_PERMISSION_LIST;
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId,Picture::getSpaceId,Picture::getUserId)
                    .one();
            if (picture == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人和管理员可操作
            if (spaceId == null){
                if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)){
                    return ADMIN_PERMISSION_LIST;
                }else {
                    // 不是自己的图片,只能查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 获取space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        // 根据 space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()){
            // 私有空间，仅本人和管理员可操作
            if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) return ADMIN_PERMISSION_LIST;
            else return new ArrayList<>();
        }else{
            // 团队空间，查询当前用户的 spaceUser
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId,spaceId)
                    .eq(SpaceUser::getUserId,userId)
                    .one();
            if (spaceUser == null) return new ArrayList<>();
            return spaceUserAuthManager.getPermissionByRole(spaceUser.getSpaceRole());
        }
    }

    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}


