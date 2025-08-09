package com.liyiwei.picturebase.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest{

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 时间维度
     * 可选值：day, week, month
     */
    private String timeDimension;

}


