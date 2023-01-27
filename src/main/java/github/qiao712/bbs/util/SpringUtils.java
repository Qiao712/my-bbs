package github.qiao712.bbs.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 方便在非Bean类中获取Bean对象
 */
@Component
public class SpringUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    //注入到静态变量
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName){
        try{
            return applicationContext.getBean(beanName);
        }catch (RuntimeException e) {
            return null;
        }
    }

    public static Object getBean(Class<?> beanType){
        return applicationContext.getBean(beanType);
    }
}
