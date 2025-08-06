package com.liyiwei.picturebase.service;


import com.liyiwei.picturebase.model.entity.User;
import com.liyiwei.picturebase.model.vo.analyze.SpaceAnalyzeRequest;
import org.springframework.stereotype.Service;

@Service
public interface SpaceAnalyzeService  {

    /**
     * 校验空间分析权限
     * @param spaceAnalyzeRequest
     * @param loginUser
     */
    void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);
}
