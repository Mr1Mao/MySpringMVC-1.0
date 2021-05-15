package com.mao.springmvc.servlet;

import com.mao.springmvc.annotations.*;
import com.mao.springmvc.utils.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @auth0r Mao
 * @date 2021/5/14 16:21
 */
public class MDispatcherServlet extends HttpServlet {
    //保存配置文件的内容
    private Properties contextConfig = new Properties();
    //存放bean容器
    private Map<String,Object> beanContext= new HashMap<>();
    //用于存放 扫描到的组件路径
    private List<String> classNames = new ArrayList<>();
    //用户存放 controller 中对应 url的方法
//    private Map<String,Method> handlerMapping = new HashMap<>();
    //用户村饭 Handler
    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //初始化扫描到的类，并放入ioc容器中
        doInstance();
        //依赖注入
        doAutowired();
        //初始化HandlerMapping
        initHandlerMapping();

        System.out.println("框架初始化完成");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //委派模式
        try {
            doDispatch(req,resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将 req 和 resp 分发给 各个 controller的方法处理
     * @param req
     * @param resp
     */
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        Handler handler = getHandler(req);
        //放回的 handler为空则 放回404
        if (handler == null) {
            resp.getWriter().write("404 not find");
        }

        Method method = handler.getMethod();
        //获取 controller中对应方法的 参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求中的 参数
        Map<String, String[]> parameterMap = req.getParameterMap();

        //最终所需要传递给 方法的 参数集合
        Object[] params= new Object[parameterTypes.length];

        for (Map.Entry<String,String[]> param:parameterMap.entrySet()) {
            //parameterMap.get(paramName) 放回的为一个数组
            String value = Arrays.toString(param.getValue())
                    //将括号 删除
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s",",");
            //根据 请求中参数的名字查找 所在的参数在 方法的形参中所在的位置
            Integer index = handler.getParamIndexMapping().get(param.getKey());
            //将参数类型转换 保存给 params
            params[index] = convert(parameterTypes[index],value);
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            //如果是HttpServletRequest类型 将req 传递过去
            if(parameterTypes[i] == HttpServletRequest.class){
                params[i] = req;
                continue;
            }
            //判断 HttpServletResponse
            if (parameterTypes[i] == HttpServletResponse.class){
                params[i] = resp;
                continue;
            }
        }
        Object result = method.invoke(handler.getController(), params);
        //根据返回值 返回参数
        if(result == null || result instanceof Void){
            return ;
        }
        resp.getWriter().write(result.toString());
    }

    /**
     * 通过url中的正则 去匹配对应的handler
     * @param req
     * @return handler
     */
    public Handler getHandler(HttpServletRequest req){
        if (handlerMapping.isEmpty()) return null;

        //获取请求路径
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        requestURI = requestURI.replaceAll(contextPath,"").replaceAll("/+","/");

        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(requestURI);
            //如果没有匹配上 跳过
            if(!matcher.matches()){
                continue;
            }

            return handler;
        }
        return  null;
    }

    /**
     * 请求中的参数默认为 String 将他转换为 controller中方法 对应参数的类型
     * @param type
     * @param value
     * @return
     */
    private Object convert(Class type,String value){
        if(type == Integer.class){
            return Integer.valueOf(value);
        }else if(type == Double.class){
            return Double.valueOf(value);
        }
        //默认放回 String
        return value;
    }

    /**
     * 处理 请求路径 和 controller 方法的对应关系
     */
    private void initHandlerMapping() {
        if(beanContext.isEmpty()) return;

        for (Map.Entry<String,Object> bean:beanContext.entrySet()) {
            Class<?> clazz = bean.getValue().getClass();
            // 操作Controller
            if(clazz.isAnnotationPresent(MController.class)){
                MRequestMapping requestMapping = clazz.getAnnotation(MRequestMapping.class);
                String basePath = requestMapping.value().trim();
                //获得该类的方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
//                    未标有 @MRequestMapping的方法跳过
                    if(!method.isAnnotationPresent(MRequestMapping.class))continue;

                    MRequestMapping annotation = method.getAnnotation(MRequestMapping.class);
                    //获取方法上的path  "/+" 如果有多个"/" 则转化为一个"/"
                    String regex = ("/" + basePath + "/" + annotation.value()).replaceAll("/+","/");
                    Pattern pattern = Pattern.compile(regex);
                    //存放在 handlerMapping中
                    handlerMapping.add(new Handler(bean.getValue(),method,pattern));
                }
            }else {
                continue;
            }

        }
    }

    /**
     * 对beanContext中的 bean进行 依赖注入
     */
    private void doAutowired() {
        if (beanContext.isEmpty()) return;

        for (Map.Entry<String,Object> bean:beanContext.entrySet()) {
            //获取该类中 所有的属性
            Field[] fields = bean.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                //判断属性中是否有@MAutowired注解
                if(field.isAnnotationPresent(MAutowired.class)){
                    //beanName 默认为 该属性类型的 小写开头
                    String beanName = toLowerFirstCase(field.getType().getSimpleName());
                    MAutowired annotation = field.getAnnotation(MAutowired.class);
                    String value = annotation.value().trim();
                    //判断注解中的 value是否为空
                    if(!"".equals(value)){
                        beanName = value;
                    }
                    //从容器中获取
                    Object object = beanContext.get(beanName);
                    //暴力访问私有属性
                    field.setAccessible(true);

                    try {
                        //为该属性 进行 注入
                        field.set(bean.getValue(),object);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }else{
                    continue;
                }

            }
        }
    }

    /**
     * 通过 classNames中 中获得的.class文件
     * 对有相应接口的类 进行实例化
     */
    private void doInstance() {
        //如果为空直接返回
        if(classNames.isEmpty()) return;
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                //是否为被@MService注解的类
                if(clazz.isAnnotationPresent(MService.class)){
                    MService annotation = clazz.getAnnotation(MService.class);
                    Object instance = clazz.newInstance();
                    //获取改类的名字
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    //获取注解中的value
                    String value = annotation.value();
                    //如果value 去掉头尾空格 不等于默认值
                    if(!"".equals(value.trim())){
                        beanName = value.trim();
                    }
                    beanContext.put(beanName,instance);

                    for (Class<?> clazzInterface : clazz.getInterfaces()) {
                        //如果 该接口的实例已在容器中存在 则抛出异常
                        if(beanContext.containsKey(clazzInterface.getName())){
                            throw new Exception("The"+clazzInterface.getName() + "is exists");
                        }
                        //将 接口类型作为 key存入 bean容器中
                        beanContext.put(clazzInterface.getName(),instance);
                    }
                    //被@MController注解的类
                } else if (clazz.isAnnotationPresent(MController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    beanContext.put(beanName,instance);
                }else{
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                //存入 classNames中
                classNames.add(className);
            }
        }

    }

    /**
     * 加载配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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
    }
    //将首字母修改为小写
    public String toLowerFirstCase(String str){
        char[] chars = str.toCharArray();
//        将首字母的ASCII码增加32
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
