package com.liyiwei.picturebase.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("审核中", 0),
    PASS("审核通过", 1),
    REJECT("审核拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {return null;}
        for (PictureReviewStatusEnum e : PictureReviewStatusEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }

}
