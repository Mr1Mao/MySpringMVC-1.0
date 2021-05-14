package com.mao.springmvc.annotations;

import java.lang.annotation.*;

/**
 * @auth0r Mao
 * @date 2021/5/14 11:07
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MRequestMapping {
    String value() default "";
}
