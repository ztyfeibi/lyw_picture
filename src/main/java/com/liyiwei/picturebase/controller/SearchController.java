package com.liyiwei.picturebase.controller;

import com.liyiwei.picturebase.common.BaseResponse;
import com.liyiwei.picturebase.common.ResultUtils;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.liyiwei.picturebase.exception.ThrowUtils;
import com.liyiwei.picturebase.model.dto.search.SearchPicturesByColorRequest;
import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.PictureVO;
import com.liyiwei.picturebase.service.PictureService;
import com.liyiwei.picturebase.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {


    @Autowired
    private UserService userService;

    @Autowired
    private PictureService pictureService;

    @PostMapping("/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPicturesByColorRequest searchRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(searchRequest == null, ErrorCode.PARAMS_ERROR);

        String picColor = searchRequest.getPicColor();
        Long spaceId = searchRequest.getSpaceId();
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> res = pictureService.searchPictureByColor(spaceId,picColor,loginUser);
        return ResultUtils.success(res);
    }
}
