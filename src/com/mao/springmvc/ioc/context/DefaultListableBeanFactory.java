package com.mao.springmvc.ioc.context;

import com.mao.springmvc.ioc.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auth0r Mao
 * @date 2021/7/10 16:51
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext{
    // 用于存放 bean定义的信息(beanDefinition)信息
    protected final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
