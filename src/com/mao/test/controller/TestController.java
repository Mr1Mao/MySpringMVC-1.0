package com.mao.test.controller;

import com.mao.springmvc.annotations.MAutowired;
import com.mao.springmvc.annotations.MController;
import com.mao.springmvc.annotations.MRequestMapping;
import com.mao.springmvc.annotations.MRequestParam;
import com.mao.test.entity.User;
import com.mao.test.service.UserService;
import com.sun.deploy.net.HttpRequest;
import com.sun.deploy.net.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @auth0r Mao
 * @date 2021/5/14 11:14
 */
@MController
@MRequestMapping("/test")
public class TestController {

    @MAutowired
    private UserService userService;

    @MRequestMapping("/password")
    public void getPassword(HttpServletRequest request, HttpServletResponse response, @MRequestParam(value = "usr") String username){
        System.out.println(username);
        String userPassword = userService.queryUserPassword(username);

        try {
            response.getWriter().write(userPassword);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MRequestMapping("/user")
    public User getUser(@MRequestParam(value = "usr") String username){
        System.out.println(username);
        User user = userService.queryUser(username);
        return user;
    }


}
