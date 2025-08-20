package com.liyiwei.picturebase.api;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.liyiwei.picturebase.exception.BusinessException;
import com.liyiwei.picturebase.exception.ErrorCode;
import com.qcloud.cos.model.ciModel.image.ImageSearchResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetImageApi {
//    public static String getPicturePageUrl(String imageUrl) {
//    }
//
//    public static String getPictureFirstUrl(String picturePageUrl) {
//        Jsoup
//    }

    /**
     * 获取图片列表
     * @param url
     * @return
     */
    public static List<ImageSearchResponse> getImageList(String url) {
        HttpResponse response = HttpUtil.createGet(url).execute();
        int code = response.getStatus();
        String body = response.body();

        if (code == 200) {
            return processResponse(body);
        }else{
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"接口调用失败");
        }
    }

    /**
     * 处理响应体
     * @param body 响应体内容
     * @return 解析后的图片搜索响应列表
     */
    private static List<ImageSearchResponse> processResponse(String body) {
        JSONObject jsonObject = new JSONObject(body);
        if (!jsonObject.containsKey("data")) throw new BusinessException(ErrorCode.OPERATION_ERROR,"未获取到data");

        JSONObject data = jsonObject.getJSONObject("data");
        if (!data.containsKey("list")) throw new BusinessException(ErrorCode.OPERATION_ERROR,"未获取到list");

        JSONArray list = data.getJSONArray("list");
        return JSONUtil.toList(list, ImageSearchResponse.class);
    }
}
