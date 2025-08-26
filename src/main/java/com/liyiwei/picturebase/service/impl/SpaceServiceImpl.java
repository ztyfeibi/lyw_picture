package com.liyiwei.picturebase.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.manager.sharding.DynamicShardingManager;
import com.liyiwei.picturebase.model.dto.space.SpaceAddRequest;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.entity.SpaceUser;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.enums.SpaceLevelEnum;
import com.liyiwei.picturebase.model.enums.SpaceRoleEnum;
import com.liyiwei.picturebase.model.enums.SpaceTypeEnum;
import com.liyiwei.picturebase.model.vo.PictureVO;
import com.liyiwei.picturebase.model.vo.UserVO;
import com.liyiwei.picturebase.model.vo.space.SpaceVO;
import com.liyiwei.picturebase.service.SpaceService;
import com.liyiwei.picturebase.mapper.SpaceMapper;
import com.liyiwei.picturebase.service.SpaceUserService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

/**
* @author 16001
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-08-03 21:39:49
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    @Autowired
    private UserService userService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private SpaceUserService spaceUserService;
    @Autowired
    private DynamicShardingManager dynamicShardingManager;

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        Integer spaceType = space.getSpaceType();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);
        // 要创建
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        // 修改数据时，如果要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /*
    todo 看不懂
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) spaceAddRequest.setSpaceName("默认空间");
        if (spaceAddRequest.getSpaceLevel() == null) spaceAddRequest.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        if (spaceAddRequest.getSpaceType() == null) spaceAddRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());

        // 将实体类和DTO进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        this.fillSpaceBySpaceLevel(space);
        // 数据校验
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);

        // 权限校验
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权限创建指定级别的空间");
        }
        //todo 只要设计事务，应手写测试异常，判断是否会回滚
        /*
         先查后插入，存在幻读可能，所以必须用事务，针对用户唯一id进行加锁，这样保证不会重复插入
         */
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                if (!userService.isAdmin(loginUser)) {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId,userId)
                            .eq(Space::getSpaceType,spaceAddRequest.getSpaceType())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR,"每个用户只能有一个空间");
                }

                //写入数据库
                boolean res = this.save(space);
                ThrowUtils.throwIf(!res,ErrorCode.OPERATION_ERROR);
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    res = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(res, ErrorCode.OPERATION_ERROR,"创建团队成员失败");
                }
                // 创建分表
                dynamicShardingManager.createSpacePictureTable(space);
                return space.getId();
            });
            // 返回结果是包装类，可以做一些处理
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (space.getUserId() == null || loginUser.getId() == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser))
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"没有空间访问权限");
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.obj2Vo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }


}




