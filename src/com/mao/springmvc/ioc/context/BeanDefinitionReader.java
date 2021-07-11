package com.mao.springmvc.ioc.context;

import com.mao.springmvc.annotations.MController;
import com.mao.springmvc.annotations.MService;
import com.mao.springmvc.ioc.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 为获取容器的入口
 * @auth0r Mao
 * @date 2021/7/10 16:54
 */
public class BeanDefinitionReader {
    //保存配置文件的内容
    private Properties contextConfig = new Properties();
    //存放包扫描的结果 类名 由于是多个配置文件 避免重复的数据写入 所以用set
    private Set<String> registerBeanClasses = new CopyOnWriteArraySet<>();
    //对应于 配置文件中的字段
    private final String SCAN_PACKAGE = "scanPackage";

    /**
     * 构造方法 解析配置文件信息
     * @param configLocations 配置文件 地址
     */
    public BeanDefinitionReader(String... configLocations) {
        //遍历 配置文件 地址数组
        for (int i = 0; i < configLocations.length; i++) {
        // 根据配置文件的地址 找到 配置文件 并转换成文件流的形式
            InputStream resource = this.getClass().getClassLoader().getResourceAsStream(configLocations[i].replace("classpath:",""));
            try {
                contextConfig.load(resource);
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if (resource != null){
                    //关闭流
                    try {
                        resource.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //扫描包 路径
            doScanner(contextConfig.getProperty(SCAN_PACKAGE));
        }

    }

    /**
     * 加载 beanDefinition
     * @return
     */
    public List<BeanDefinition> loadBeanDefinitions(){
        //用来 临时存放 BeanDefinition的结果集
        ArrayList<BeanDefinition> beanDefinitions = new ArrayList<>();
        //此处遍历的是 相关目录下的 所有类名
        for (String className : registerBeanClasses) {
            try {
                //通过名字 获取class
                Class<?> beanClass = Class.forName(className);
                //如果是接口 则跳过
                if (beanClass.isInterface()) {
                    continue;
                }
                /**
                 * 解释
                 * beanClass.getSimpleName() ----> User
                 * beanClass.getName() -----> com.xx.User
                 */
                //将 创建出来的 BeanDefinition 存放在 临时的结果集 list中
                beanDefinitions.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                //获取该类 实现的 所有接口
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    //将这些 接口都注册到 beanDefinition List中
                    beanDefinitions.add(doCreateBeanDefinition(anInterface.getName(),beanClass.getName()));
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return beanDefinitions;
    }

    /**
     * 创建 BeanDefinition
     * @param factoryBeanName
     * @param beanClassName
     * @return
     */
    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    /**
     * 扫描项目目录下的所有文件 并添加到ioc容器
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        //转换文件路径格式
        String path = "/" + scanPackage.replace(".","/");
        //通过类加载器，获取该目录的绝对路径
        URL url = this.getClass().getClassLoader().getResource(path);
        File classpath = new File(url.getFile());
        for (File file : classpath.listFiles()) {
//            判断是否为包目录
            if (file.isDirectory()){
//               递归该方法操作
                doScanner(scanPackage + "." + file.getName());
            }else{
                //如果外文件不是.class文件则跳过
                if(!file.getName().endsWith(".class")){continue;}
                //得到.class 文件路径修改格式
                String className = scanPackage + "." + file.getName().replace(".class","");
                //存入 registerBeanClasses中
                registerBeanClasses.add(className);
            }
        }

    }

    //将首字母修改为小写
    public String toLowerFirstCase(String str){
        char[] chars = str.toCharArray();
//        将首字母的ASCII码增加32
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getContextConfig() {
        return contextConfig;
    }
}
