package com.liyiwei.picturebase.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyiwei.picturebase.auth.stpUtil.StpKit;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.model.dto.user.UserQueryRequest;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.enums.UserRoleEnum;
import com.liyiwei.picturebase.model.vo.LoginUserVO;
import com.liyiwei.picturebase.model.vo.UserVO;
import com.liyiwei.picturebase.service.UserService;
import com.liyiwei.picturebase.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.liyiwei.picturebase.model.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 16001
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-06-26 16:41:38
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if (StringUtils.isBlank(userAccount) || StringUtils.isBlank(userPassword) || StringUtils.isBlank(checkPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if (userAccount.length() < 4) throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        if (userPassword.length() < 6) throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        if (!userPassword.equals(checkPassword)) throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");

        //2.检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");

        //3.加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //4.创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserName("名字");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) throw new BusinessException(ErrorCode.OPERATION_ERROR,"注册失败 DataBase error");

        return user.getId();
    }

    @Override
    public String getEncryptedPassword(String userPassword) {
        final String SALT = "yupi";
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1、校验
        if (StringUtils.isBlank(userAccount) || StringUtils.isBlank(userPassword))
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        if (userAccount.length() < 4)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号错误");
        if (userPassword.length() < 6)
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");

        //2、加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        //3、查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("login failed，账号或密码错误，userAccount:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在，或账号密码错误");
        }

        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 记录用户登录状态到Sa-token，注意保持该用户信息与SpringSession中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) return null;

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 从session中获取用户信息,判断是否已经登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null)
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);

        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //先判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");

        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return false;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) return new ArrayList<>();
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) return null;

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String userProfile = userQueryRequest.getUserProfile();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);

        return queryWrapper;
    }
}




