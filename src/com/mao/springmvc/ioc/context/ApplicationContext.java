package com.mao.springmvc.ioc.context;

import com.mao.springmvc.annotations.MAutowired;
import com.mao.springmvc.annotations.MController;
import com.mao.springmvc.annotations.MService;
import com.mao.springmvc.ioc.beans.BeanDefinition;
import com.mao.springmvc.ioc.beans.BeanWrapper;
import com.mao.springmvc.ioc.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApplicationContext 在Spring中 为获取ioc容器的入口
 * @auth0r Mao
 * @date 2021/7/10 16:52
 */
public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    //本地配置信息
    private String[] configLocations;
    private  BeanDefinitionReader reader;

    //单例 IOC 容器 缓存
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>();
    //用来储存 所有被代理过的beanWrapper对象
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    /**
     * 构造方法 传入本地配置地址
     */
    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            //调用 refresh方法 刷新容器
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重写 父类中的 refeash方法
     * @throws Exception
     */
    @Override
    public void refresh() throws Exception {
        //读取配置文件
        reader = new BeanDefinitionReader(configLocations);
        //加载配置文件，扫描相关的类，把他们封装成BeanDefinition
        List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        // 注册 beanDefinition 至 容器中
        doRegisterBeanDefinition(beanDefinitions);
        //初始化非懒加载的类
        doAutowrited();
    }

    /**
     * 初始化 非懒加载的类
     */
    private void doAutowrited() {
        //遍历父类的 容器
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            System.out.println("======="+beanName);
            if (!beanDefinitionEntry.getValue().isLazyInit()){
                try {
                    //调用 getBean() 获得 和创建 bean对象
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * beanDefinition 的注册
     * @param beanDefinitions
     * @throws Exception
     */
    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            // 如果该类在 容器中存在 则抛出异常
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The "+beanDefinition.getFactoryBeanName()+"is exist");
            }
            //添加至 父类的ioc容器中
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }

    @Override
    public Object getBean(String beanName) throws Exception {
        try {

            //从 父类的容器中 取出 对应的beanDefinition 对象
            BeanDefinition beanDefinition = super.beanDefinitionMap.get(beanName);
            // 根据 beanDefinition(bean定义信息) 去实例化 对象
            Object instance = instantiateBean(beanDefinition);
            // 将实例对象 封装在 beanWrapper 中
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            // 存放到 beanWrapper 容器当中
            factoryBeanInstanceCache.put(beanName,beanWrapper);
            //填充属性
            populateBean(beanName,instance);
            //放回 Bean
            return factoryBeanInstanceCache.get(beanName).getWrappedInstance();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 填充属性 方法
     * @param beanName
     * @param instance
     */
    private void populateBean(String beanName, Object instance) {
        //获取该实例的 class
        Class<?> clazz = instance.getClass();

        // 如果 没有 以下注解的都排除
        if(!(clazz.isAnnotationPresent(MController.class)||clazz.isAnnotationPresent(MService.class))){
            return;
        }
        // 获取该类 所有的属性
        Field[] declaredFields = clazz.getDeclaredFields();
//        System.out.println(beanName+": 全部属性：" + Arrays.toString(declaredFields));
        for (Field field : declaredFields) {
            // 跳过 没有@MAutowired 注解的属性
            if (!field.isAnnotationPresent(MAutowired.class)) {
                continue;
            }
            MAutowired annotation = field.getAnnotation(MAutowired.class);
            //获取 该注解上的value 作为 bean名字
            String autowiredBeanName = annotation.value().trim();
            //如果 value 为空 则获取该类型 的名字 作为名字
            if("".equals(autowiredBeanName)){
                autowiredBeanName = toLowerFirstCase(field.getType().getSimpleName());
            }
            // 强行访问私有属性
            field.setAccessible(true);

            try {
//                System.out.println("当前类："+ beanName +"当前属性：" + field.getName());
                if (factoryBeanInstanceCache.get(autowiredBeanName) == null) {
//                    System.out.println("空："+autowiredBeanName);
                    continue;
                }
                //为 该属性设置内容
                field.set(instance,factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

    }

    /**
     * 实例化 Bean
     * @param beanDefinition
     */
    private Object instantiateBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        if (factoryBeanObjectCache.containsKey(className)) {
            // 如果 factoryBeanObjectCache 中有这个对象 则直接取出该对象 即可
            instance = factoryBeanObjectCache.get(className);
        }else{
            //如果 不存在该对象 则需要新创建这个对象
            try {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        //返回 对象
        return instance;
    }

    @Override
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {

        return beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }

    //将首字母修改为小写
    public String toLowerFirstCase(String str){
        char[] chars = str.toCharArray();
//        将首字母的ASCII码增加32
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
