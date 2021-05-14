package com.mao.springmvc.annotations;

import java.lang.annotation.*;

/**
 * @auth0r Mao
 * @date 2021/5/14 11:00
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MService {
    String value() default "";
}
