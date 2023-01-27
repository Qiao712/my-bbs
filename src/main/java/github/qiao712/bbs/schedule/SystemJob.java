package github.qiao712.bbs.schedule;

import github.qiao712.bbs.util.SpringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Quartz任务
 * 根据JobDetail的JobDataMap中，invokeTarget值执行相应bean的方法
 */
@Slf4j
@DisallowConcurrentExecution    //禁止并发
public class SystemJob implements Job {
    public final static String INVOKE_TARGET = "invokeTarget";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Invocation invocation;
        try{
            invocation = parseInvokeTarget((String) context.getJobDetail().getJobDataMap().get(INVOKE_TARGET));
        }catch (Exception e){
            throw new JobExecutionException("invokeTarget解析失败", e);
        }

        //执行
        try {
            Object result = invocation.invoke();
            log.info("定时任务执行, 返回值{}", result);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new JobExecutionException("定时任务执行中抛出异常", e);
        }
    }

    /**
     * 检查invokeTarget是否合法
     * @throws IllegalArgumentException 若不合法抛出
     */
    public static void checkInvokeTarget(String invokeTarget){
        parseInvokeTarget(invokeTarget);
    }

    @AllArgsConstructor
    private static class Invocation {
        Object bean;
        Method method;
        Object invoke() throws InvocationTargetException, IllegalAccessException {
            return method.invoke(bean);
        }
    }

    /**
     * 解析InvokeTarget获取Bean对象和Method
     */
    private static Invocation parseInvokeTarget(String invokeTarget){
        //解析调用目标
        //描述 目标函数 (不支持参数), 格式:
        // @beanName::function
        // beanType::function
        String beanName = null;
        String beanType = null;
        String methodName = null;
        if(invokeTarget == null){
            throw new IllegalArgumentException("invokeTarget 为 null");
        }
        String[] split = invokeTarget.split("::");
        if(split.length == 2){
            if(split[0].startsWith("@")){
                beanName = split[0].substring(1);
            }else{
                beanType = split[0];
            }

            methodName = split[1];
        }else{
            throw new IllegalArgumentException("invokeTarget格式错误");
        }

        //获取Bean
        Object bean = null;
        if(beanName != null) {
            //根据bean名查找Bean
            bean = SpringUtils.getBean(beanName);
        }
        if(beanType != null) {
            //根据bean类型查找Bean
            try {
                Class<?> clazz = Class.forName(beanType);
                bean = SpringUtils.getBean(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("找不到类型" + beanType);
            }
        }
        if(bean == null){
            throw new IllegalArgumentException("无法获取Bean (name=" + beanName + " type=" + beanType + ")");
        }

        try {
            Method method = bean.getClass().getMethod(methodName);
            return new Invocation(bean, method);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("找不到函数"+methodName , e);
        }
    }
}
