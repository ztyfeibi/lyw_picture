package com.liyiwei.picturebase.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)// 表示只能用在方法上
@Retention(RetentionPolicy.RUNTIME) // 指定这个注解在运行时依然有效，JVM 加载类后可以通过反射获取到它。 如果没有这个设置，注解可能只在源码或编译期有效，运行时就无法读取。
public @interface AuthCheck {

    /**
     * 必须有某个角色
     */
    String mustRole() default "";
}
