package com.mao.test.controller;


import com.mao.springmvc.annotations.MController;
import com.mao.springmvc.annotations.MRequestMapping;

/**
 * @auth0r Mao
 * @date 2021/7/11 16:04
 */
@MController
@MRequestMapping("")
public class HelloController {
    @MRequestMapping("")
    public String hello(){
        return "hello";
    }
}
