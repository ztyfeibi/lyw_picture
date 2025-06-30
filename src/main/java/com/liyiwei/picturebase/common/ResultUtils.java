package com.liyiwei.picturebase.common;

import com.liyiwei.picturebase.exception.ErrorCode;

public class ResultUtils {

    /**
     * 成功
     *
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 失败
     *
     * @param errorCode
     * @return
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     *
     * @param code
     * @param message
     * @return
     */
    public static BaseResponse<?> error(int code,String message) {
        return new BaseResponse<>(code,message);
    }

    /**
     *
     * @param errorCode
     * @param message
     * @return
     */
    public static BaseResponse<?> error(ErrorCode errorCode,String message) {
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }

}
