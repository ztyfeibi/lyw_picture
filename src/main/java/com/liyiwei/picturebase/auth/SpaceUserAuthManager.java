package com.liyiwei.picturebase.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.liyiwei.picturebase.auth.model.SpaceUserAuthConfig;
import com.liyiwei.picturebase.auth.model.SpaceUserRole;
import com.liyiwei.picturebase.service.SpaceUserService;
import com.liyiwei.picturebase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpaceUserAuthManager {

    @Autowired
    private SpaceUserService spaceUserService;

    @Autowired
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    // TODO JSONUTIL
    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public List<String> getPermissionByRole(String spaceUserRole){
        if (StrUtil.isBlank(spaceUserRole)){
            return new ArrayList<>();
        }
        // 获取角色对应的权限列表
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        if (role == null){
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

}
