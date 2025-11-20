package com.ccc.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface logger {
    int value() default 1;
}
