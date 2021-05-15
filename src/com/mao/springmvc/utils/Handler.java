package com.mao.springmvc.utils;

import com.mao.springmvc.annotations.MRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 记录 Controller 中的 RequestMapping 和RUL的对应关系
 * @auth0r Mao
 * @date 2021/5/15 14:29
 */
public class Handler {
    //方法对应的实例
    private Object controller;
    private Method method;
    private Pattern pattern;
    private Map<String,Integer> paramIndexMapping;

    public Handler(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
        paramIndexMapping = new HashMap<>();

        putParamIndexMapping(method);
    }

    /**
     * 讲 method中对应的参数顺序添加到 paramIndexMapping 中
     * @param method
     */
    private void putParamIndexMapping(Method method) {
//        获得该方法中的所有参数上的注解
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if(annotation instanceof MRequestParam){
                    //获取标签上的value值(参数名称)
                    String paramName = ((MRequestParam) annotation).value();
                    if(!"".equals(paramName.trim())){
//                        根据参数名称保存参数的对应位置
                        paramIndexMapping.put(paramName,i);
                    }

                }

            }
        }
//        添加HttpServletRequest,HttpServletResponse 参数类型的对应位置
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }


    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
        this.paramIndexMapping = paramIndexMapping;
    }
}
