package com.liyiwei.picturebase.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;
    private String text;
    private long maxCount;
    private long maxSize; // 单位：字节
}
