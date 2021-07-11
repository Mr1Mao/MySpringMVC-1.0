package com.mao.springmvc.ioc.core;

/**
 * @auth0r Mao
 * @date 2021/7/10 16:17
 */
public interface BeanFactory {

    /**
     * 通过 beanName 从IOC容器中 去获取 实例
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 通过 className 去获取实例
     * @param beanClass
     * @return
     * @throws Exception
     */
    public Object getBean(Class<?> beanClass) throws Exception;
}
