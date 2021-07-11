package com.mao.springmvc.ioc.beans;

/**
 * @auth0r Mao
 * @date 2021/7/10 16:50
 */
public class BeanWrapper {
    //原生对象实例
    private Object wrappedInstance;
    //原生对象的 class
    private Class wrappedClass;

    public BeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
        this.wrappedClass = wrappedInstance.getClass();
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public void setWrappedInstance(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Class getWrappedClass() {
        return wrappedClass;
    }

    public void setWrappedClass(Class wrappedClass) {
        this.wrappedClass = wrappedClass;
    }

}
