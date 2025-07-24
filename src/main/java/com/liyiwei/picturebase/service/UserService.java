package com.liyiwei.picturebase.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyiwei.picturebase.model.dto.user.UserQueryRequest;
import com.liyiwei.picturebase.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liyiwei.picturebase.model.vo.LoginUserVO;
import com.liyiwei.picturebase.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* 接口要写注释，这样就不用跳到具体实现去看了
*/
public interface UserService extends IService<User> {

    /**
     * 判断用户是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     *用户注册
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密后的密码
     * @param userPassword 原始密码
     * @return  加密后的密码
     */
    String getEncryptedPassword(String userPassword);

    /**
     * 用户登录
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param request   请求方便设置cookie
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);


    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取用户脱敏信息
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
