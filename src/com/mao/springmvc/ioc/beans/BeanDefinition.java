package com.mao.springmvc.ioc.beans;

/**
 * bean定义
 * @auth0r Mao
 * @date 2021/7/10 16:49
 */
public class BeanDefinition {

    //原生Bean的全类名
    private String beanClassName;
    //是否懒加载
    private boolean lazyInit = false;
    //保存 beanName,即 Ioc容器中储存的Key
    private String factoryBeanName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
