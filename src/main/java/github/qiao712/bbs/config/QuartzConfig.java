package github.qiao712.bbs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

@Configuration
public class QuartzConfig {
    /**
     * Quartz相关配置
     * 将创建一个Quartz的Scheduler对象
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("/quartz.properties"));    //quartz的配置文件
        schedulerFactoryBean.setDataSource(dataSource);                                         //设置数据源
        schedulerFactoryBean.setAutoStartup(true);      //自动启动
        return schedulerFactoryBean;
    }
}
