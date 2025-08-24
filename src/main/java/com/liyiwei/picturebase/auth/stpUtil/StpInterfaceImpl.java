package com.liyiwei.picturebase.auth.stpUtil;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限吗集合
     */
    @Override
    public List<String> getPermissionList(Object o, String s) {
        List<String> list = new ArrayList<String>();
        list.add("");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object o, String s) {
        List<String> list = new ArrayList<>();
        list.add("");
        return list;
    }
}
