package com.liyiwei.picturebase.common;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class DeleteRequest implements Serializable {

    private Long id;

    private static final long serialVersionUID = 1L;
}
