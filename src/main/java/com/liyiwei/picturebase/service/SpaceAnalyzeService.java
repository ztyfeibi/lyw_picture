package com.liyiwei.picturebase.service;


import com.liyiwei.picturebase.model.dto.space.analyze.*;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.space.analyze.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SpaceAnalyzeService  {

    /**
     * 校验空间分析权限
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 获取空间使用分析数据
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类分析数据
     * @param categoryAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest categoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间标签分析数据
     * @param tagAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest tagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小分析数据
     * @param sizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest sizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户分析数据
     * @param userAnalyzeRequest
     * @param loginUser
     * @return
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest userAnalyzeRequest, User loginUser);

}
