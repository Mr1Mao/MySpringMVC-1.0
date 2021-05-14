package com.mao.springmvc.annotations;

import java.lang.annotation.*;

/**
 * @auth0r Mao
 * @date 2021/5/14 11:09
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MRequestParam {
    String value() default "";
}
