package com.mao.springmvc.annotations;

import java.lang.annotation.*;

/**
 * @auth0r Mao
 * @date 2021/5/14 11:05
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MAutowired {
    String value() default "";
}
