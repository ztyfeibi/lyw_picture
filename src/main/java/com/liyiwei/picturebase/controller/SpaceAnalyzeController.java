package com.liyiwei.picturebase.controller;

import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.dto.space.analyze.*;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.space.analyze.*;
import com.liyiwei.picturebase.service.SpaceAnalyzeService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Autowired
    private SpaceAnalyzeService spaceAnalyzeService;

    @Autowired
    private UserService userService;

    /**
     * 获取空间使用分析
     * @param analyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest analyzeRequest,
            HttpServletRequest request) {

        ThrowUtils.throwIf(analyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse analyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(analyzeRequest, loginUser);
        return ResultUtils.success(analyzeResponse);
    }

    /**
     * 获取空间分类分析
     * @param categoryAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest categoryAnalyzeRequest,
            HttpServletRequest request) {

        ThrowUtils.throwIf(categoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> res = spaceAnalyzeService.getSpaceCategoryAnalyze(categoryAnalyzeRequest, loginUser);
        return ResultUtils.success(res);
    }

    /**
     * 获取空间标签分析
     * @param spaceTagAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    /**
     * 获取空间大小分析
     * @param spaceSizeAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    /**
     * 获取空间用户分析
     * @param spaceUserAnalyzeRequest
     * @param request
     * @return
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }


}
