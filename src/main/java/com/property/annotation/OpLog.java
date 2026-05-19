package com.property.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {
    /** 模块名，写入 target 字段 */
    String module();

    /** 操作描述，写入 action 字段 */
    String action();
}
