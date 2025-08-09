package com.liyiwei.picturebase.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.dto.space.analyze.*;
import com.liyiwei.picturebase.model.entity.Picture;
import com.liyiwei.picturebase.model.entity.Space;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.space.analyze.*;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.service.SpaceAnalyzeService;
import com.liyiwei.picturebase.service.SpaceService;
import com.liyiwei.picturebase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class SpaceAnalyzeServiceImpl implements SpaceAnalyzeService {

    @Autowired
    private UserService userService;
    @Autowired
    private SpaceService spaceService;
    @Autowired
    private PictureService pictureService;

    @Override
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // check auth
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()){
            // 全空间分析或者公共图库权限校验: 仅管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,"无权访问");
        }else{
            // 私有
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId==null || spaceId <= 0,ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
            spaceService.checkSpaceAuth(loginUser,space);
        }
    }

    // todo
    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null,ErrorCode.PARAMS_ERROR);

        // 如果是公共图库或者全空间分析 仅管理员
        if (spaceUsageAnalyzeRequest.isQueryPublic() || spaceUsageAnalyzeRequest.isQueryAll()){
            boolean isAdmin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(!isAdmin, ErrorCode.NO_AUTH_ERROR, "无权访问公共图库或全空间分析");

            // 统计公共图库的资源使用
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            if (!spaceUsageAnalyzeRequest.isQueryAll()) queryWrapper.isNull("spaceId");
            List<Object> picObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long picSize = picObjList.stream().mapToLong(res->res instanceof Long ? (Long) res : 0L).sum();
            long picCount = picObjList.size();

            // 封装放回结果
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(picSize);
            spaceUsageAnalyzeResponse.setUsedCount(picCount);

            // 公共图库没有上限和比例
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);

            return spaceUsageAnalyzeResponse;
        }

        // 查询指定空间
        Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
        ThrowUtils.throwIf(spaceId==null || spaceId <= 0,ErrorCode.PARAMS_ERROR);
        // 获取空间信息
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space==null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        // 校验权限
        spaceService.checkSpaceAuth(loginUser,space);
        // 构造返回结果
        SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
        response.setUsedSize(space.getTotalSize());
        response.setMaxSize(space.getMaxSize());
        // 后端直接计算百分比
        double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
        response.setCountUsageRatio(sizeUsageRatio);
        response.setUsedCount(space.getTotalCount());
        response.setMaxCount(space.getMaxCount());
        double countusageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
        response.setCountUsageRatio(countusageRatio);

        return response;
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest categoryAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(categoryAnalyzeRequest == null,ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(categoryAnalyzeRequest, loginUser);

        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(categoryAnalyzeRequest, queryWrapper);

        queryWrapper.select("category AS category",
                "COUNT(*) AS count",
                "SUM(picSize) AS totalSize")
                .groupBy("category");

        // 执行查询，转换结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(res -> {
                    String category = (String) res.get("category") != null ? res.get("category").toString() : "未分类";
                    Long count = ((Number) res.get("count")).longValue();
                    Long totalSize = ((Number) res.get("totalSize")).longValue();
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest tagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(tagAnalyzeRequest == null,ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(tagAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(tagAnalyzeRequest, queryWrapper);

        // 查询符合条件的所有标签
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 合并所有标签并统计次数
        Map<String,Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagJson -> JSONUtil.toList(tagJson,String.class).stream())
                .collect(Collectors.groupingBy(tag->tag, Collectors.counting()));

        // 转换为响应对象列表
        return tagCountMap.entrySet().stream()
                .sorted((e1,e2)-> Long.compare(e2.getValue(),e1.getValue())) // 按照数量降序排序
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest sizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(sizeAnalyzeRequest == null,ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(sizeAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(sizeAnalyzeRequest, queryWrapper);

        // 查询符合条件的所有图片大小
        queryWrapper.select("picSize");
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .collect(Collectors.toList());

        // 定义分段范围，注意使用有序map
        Map<String,Long> sizeRangeMap = new LinkedHashMap<>();
        sizeRangeMap.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRangeMap.put("100KB-500KB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        sizeRangeMap.put("500KB-1MB", picSizeList.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        sizeRangeMap.put(">1MB", picSizeList.stream().filter(size -> size >= 1 * 1024 * 1024).count());

        // 转换为响应对象列表
        return sizeRangeMap.entrySet()
                .stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest userAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(userAnalyzeRequest == null,ErrorCode.PARAMS_ERROR);
        checkSpaceAnalyzeAuth(userAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(userAnalyzeRequest, queryWrapper);
        Long userId = userAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId),"userId",userId);

        // 分析维度：每日、每周、每月
        String dimension = userAnalyzeRequest.getTimeDimension();
        switch (dimension){
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period","COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period","COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) AS period","COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度: " + dimension);
        }

        queryWrapper.groupBy("period").orderByAsc("period");

        List<Map<String, Object>> resultList = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return resultList.stream()
                .map(res -> {
                    String period = res.get("period").toString();
                    Long count = ((Number) res.get("count")).longValue();
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 填充queryWrapper
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

}
