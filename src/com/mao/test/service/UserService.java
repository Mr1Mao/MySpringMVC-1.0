package com.mao.test.service;

import com.mao.springmvc.annotations.MService;
import com.mao.test.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @auth0r Mao
 * @date 2021/5/14 16:11
 */
@MService
public class UserService {

    public String queryUserPassword(String username){
        //模拟数据库查找的消息
        List<User> userList = new ArrayList<>();
        userList.add(new User("Mao","123456"));
        userList.add(new User("zhangsan","654321"));
        userList.add(new User("lisi","147852"));
        String res = "the user does not exist";
        for (User user : userList) {
            if(user.getUsername().equals(username)){
                res = user.getPassword();
            }
        }
        return res;
    }

}
