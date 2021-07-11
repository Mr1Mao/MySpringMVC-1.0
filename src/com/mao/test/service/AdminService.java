package com.mao.test.service;

import com.mao.springmvc.annotations.MService;

/**
 * @auth0r Mao
 * @date 2021/7/11 21:05
 */
@MService
public class AdminService {
    public String hello(){
        return "AdminService";
    }
}
