package com.liyiwei.picturebase.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.entity.SpaceUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceUserAuthContext {

    /**
     * 临时参数，不同请求对应的id可能不同
     */
    private Long id;

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 空间用户id
     */
    private Long spaceUserId;

    /**
     * 图片信息
     */
    private Picture picture;

    /**
     * 空间信息
     */
    private Space space;

    /**
     * 空间用户信息
     */
    private SpaceUser spaceUser;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 从请求中获取上下文对象
     */
    public SpaceUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authRequest;
        // 兼容 get 和 post 操作
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = getBody(request);
            authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        } else {
            Map<String, String> paramMap = getParamMap(request);
            authRequest = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        // 根据请求路径区分 id 字段的含义
        Long id = authRequest.getId();
        if (ObjUtil.isNotNull(id)) {
            String requestUri = request.getRequestURI();
            String partUri = requestUri.replace(contextPath + "/", "");
            String moduleName = StrUtil.subBefore(partUri, "/", false);
            switch (moduleName) {
                case "picture":
                    authRequest.setPictureId(id);
                    break;
                case "spaceUser":
                    authRequest.setSpaceUserId(id);
                    break;
                case "space":
                    authRequest.setSpaceId(id);
                    break;
                default:
            }
        }
        return authRequest;
    }

    // 自己实现 getBody 方法
    public static String getBody(ServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            return IoUtil.read(reader);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    // 自己实现 getParamMap 方法
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap();

        for(Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), ArrayUtil.join(entry.getValue(), ","));
        }
        return params;
    }

    public static Map<String, String[]> getParams(ServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

}
